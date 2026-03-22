package io.qpointz.mill.persistence.metadata.jpa.adapters

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetScopeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Optional

/**
 * [MetadataRepository] adapter backed by JPA Spring Data repositories.
 *
 * Handles the two-level facet map stored as normalised rows in `metadata_facet_scope`:
 * - Outer key: full Mill facet-type URN
 * - Inner key: full Mill scope URN (resolved via [MetadataScopeJpaRepository])
 * - Value: JSON-serialised facet payload
 *
 * Scope rows are created on demand when a previously unknown scope key is encountered during
 * [save]. The global scope is always present after Flyway V4.
 *
 * @param entityRepo     the entity table JPA repository
 * @param facetScopeRepo the facet scope table JPA repository
 * @param scopeRepo      the scope table JPA repository, used to resolve or create scope rows
 */
@Transactional
class JpaMetadataRepository(
    private val entityRepo: MetadataEntityJpaRepository,
    private val facetScopeRepo: MetadataFacetScopeJpaRepository,
    private val scopeRepo: MetadataScopeJpaRepository
) : MetadataRepository {

    private val mapper: ObjectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    /**
     * Saves (inserts or updates) a [MetadataEntity] and all its facet scope data.
     *
     * The entity record is upserted first. Then for each `(facetType, scopeMap)` entry in
     * [MetadataEntity.facets], each `(scopeKey, payload)` pair is upserted in
     * `metadata_facet_scope`. Scopes are resolved or created via [resolveOrCreateScope].
     *
     * @param entity the metadata entity to persist
     */
    @Transactional
    override fun save(entity: MetadataEntity) {
        val id = entity.id ?: throw IllegalArgumentException("MetadataEntity.id must not be null")
        val now = Instant.now()

        val record = if (entityRepo.existsById(id)) {
            entityRepo.findById(id).get().also { r ->
                r.entityType = entity.type?.name ?: r.entityType
                r.schemaName = entity.schemaName
                r.tableName = entity.tableName
                r.attributeName = entity.attributeName
                r.updatedAt = now
                r.updatedBy = entity.updatedBy
            }
        } else {
            MetadataEntityRecord(
                entityId = id,
                entityType = entity.type?.name ?: "SCHEMA",
                schemaName = entity.schemaName,
                tableName = entity.tableName,
                attributeName = entity.attributeName,
                createdAt = entity.createdAt ?: now,
                updatedAt = now,
                createdBy = entity.createdBy,
                updatedBy = entity.updatedBy
            )
        }
        entityRepo.save(record)

        for ((facetType, scopeMap) in entity.facets) {
            for ((scopeKey, payload) in scopeMap) {
                val scopeEntity = resolveOrCreateScope(scopeKey)
                val payloadJson = if (payload == null) "null" else mapper.writeValueAsString(payload)
                val existing = facetScopeRepo
                    .findByEntityIdAndFacetTypeAndScopeScopeId(id, facetType, scopeEntity.scopeId)
                if (existing.isPresent) {
                    existing.get().apply {
                        this.payloadJson = payloadJson
                        this.updatedAt = now
                        this.updatedBy = entity.updatedBy
                    }
                    facetScopeRepo.save(existing.get())
                } else {
                    facetScopeRepo.save(
                        MetadataFacetScopeEntity(
                            entityId = id,
                            facetType = facetType,
                            scope = scopeEntity,
                            payloadJson = payloadJson,
                            createdAt = now,
                            updatedAt = now,
                            createdBy = entity.createdBy,
                            updatedBy = entity.updatedBy
                        )
                    )
                }
            }
        }
        log.info("Saved entity: {}", id)
    }

    /**
     * Finds a [MetadataEntity] by its identifier.
     *
     * @param id the entity identifier to look up
     * @return an [Optional] containing the reconstructed entity, or empty if not found
     */
    override fun findById(id: String): Optional<MetadataEntity> {
        val record = entityRepo.findById(id).orElse(null) ?: return Optional.empty()
        val facetRows = facetScopeRepo.findByEntityId(id)
        val scopes = scopeRepo.findAll().associateBy { it.scopeId }
        return Optional.of(toDomain(record, facetRows, scopes))
    }

    /**
     * Finds a [MetadataEntity] by its three-part location coordinates.
     *
     * @param schema    schema name coordinate; may be `null`
     * @param table     table name coordinate; may be `null`
     * @param attribute attribute name coordinate; may be `null`
     * @return an [Optional] containing the entity, or empty if not found
     */
    override fun findByLocation(schema: String?, table: String?, attribute: String?): Optional<MetadataEntity> {
        val record = entityRepo
            .findBySchemaNameAndTableNameAndAttributeName(schema, table, attribute)
            .orElse(null) ?: return Optional.empty()
        val facetRows = facetScopeRepo.findByEntityId(record.entityId)
        val scopes = scopeRepo.findAll().associateBy { it.scopeId }
        return Optional.of(toDomain(record, facetRows, scopes))
    }

    /**
     * Returns all entities of the given type.
     *
     * @param type the [MetadataType] to filter by
     * @return list of matching entities with facets loaded
     */
    override fun findByType(type: MetadataType): List<MetadataEntity> {
        val scopes = scopeRepo.findAll().associateBy { it.scopeId }
        return entityRepo.findByEntityType(type.name).map { record ->
            val facetRows = facetScopeRepo.findByEntityId(record.entityId)
            toDomain(record, facetRows, scopes)
        }
    }

    /**
     * Returns all metadata entities in the repository.
     *
     * @return list of all entities with facets loaded
     */
    override fun findAll(): List<MetadataEntity> {
        val scopes = scopeRepo.findAll().associateBy { it.scopeId }
        return entityRepo.findAll().map { record ->
            val facetRows = facetScopeRepo.findByEntityId(record.entityId)
            toDomain(record, facetRows, scopes)
        }
    }

    /**
     * Deletes the entity with the given identifier.
     *
     * No-op if the entity does not exist. Associated facet scope rows are removed by the
     * `ON DELETE CASCADE` constraint on `metadata_facet_scope`.
     *
     * @param id the entity identifier to delete
     */
    @Transactional
    override fun deleteById(id: String) {
        entityRepo.deleteById(id)
        log.info("Deleted entity: {}", id)
    }

    /**
     * Returns `true` if an entity with the given identifier exists.
     *
     * @param id the entity identifier to check
     * @return `true` if the entity is present
     */
    override fun existsById(id: String): Boolean = entityRepo.existsById(id)

    /**
     * Deletes all entities in the repository.
     *
     * Used in [io.qpointz.mill.metadata.service.ImportMode.REPLACE] mode. Associated facet
     * scope rows are removed by the `ON DELETE CASCADE` database constraint.
     */
    @Transactional
    override fun deleteAll() {
        entityRepo.deleteAll()
        log.info("Deleted all metadata entities")
    }

    /**
     * Converts a [MetadataEntityRecord] and its associated facet rows to a domain [MetadataEntity].
     *
     * For each [MetadataFacetScopeEntity], resolves the scope URN from [scopes], deserialises
     * [MetadataFacetScopeEntity.payloadJson] to `Any?`, and reconstructs the two-level facets map.
     *
     * @param record    the entity JPA record
     * @param facetRows all facet scope rows for the entity
     * @param scopes    pre-fetched map of scope URN → [MetadataScopeEntity]
     * @return the reconstructed [MetadataEntity]
     */
    internal fun toDomain(
        record: MetadataEntityRecord,
        facetRows: List<MetadataFacetScopeEntity>,
        scopes: Map<String, MetadataScopeEntity>
    ): MetadataEntity {
        val facets = mutableMapOf<String, MutableMap<String, Any?>>()
        for (row in facetRows) {
            val scopeKey = row.scope.scopeId
            val payload: Any? = if (row.payloadJson == "null") null
                                 else mapper.readValue(row.payloadJson, object : TypeReference<Any?>() {})
            facets.getOrPut(row.facetType) { mutableMapOf() }[scopeKey] = payload
        }
        return MetadataEntity(
            id = record.entityId,
            type = runCatching { MetadataType.valueOf(record.entityType) }.getOrNull(),
            schemaName = record.schemaName,
            tableName = record.tableName,
            attributeName = record.attributeName,
            facets = facets,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
            createdBy = record.createdBy,
            updatedBy = record.updatedBy
        )
    }

    /**
     * Resolves the scope entity for a given scope URN key, creating a new row if absent.
     *
     * Parses the scope key to extract the scope type and reference identifier, then either
     * returns the existing [MetadataScopeEntity] or inserts a new one.
     *
     * @param scopeKey full Mill scope URN (e.g. `"urn:mill/metadata/scope:user:alice"`)
     * @return the existing or newly created [MetadataScopeEntity]
     */
    internal fun resolveOrCreateScope(scopeKey: String): MetadataScopeEntity {
        val existing = scopeRepo.findById(scopeKey)
        if (existing.isPresent) return existing.get()

        val (scopeType, referenceId) = parseScopeKey(scopeKey)
        val scope = MetadataScopeEntity(
            scopeId = scopeKey,
            scopeType = scopeType,
            referenceId = referenceId,
            displayName = null,
            ownerId = null,
            visibility = "PUBLIC",
            createdAt = Instant.now()
        )
        return scopeRepo.save(scope)
    }

    /**
     * Parses a scope URN key into a `(scopeType, referenceId)` pair.
     *
     * @param scopeKey full Mill scope URN
     * @return a pair of scope type string and optional reference identifier
     */
    private fun parseScopeKey(scopeKey: String): Pair<String, String?> {
        if (scopeKey == MetadataUrns.SCOPE_GLOBAL) return Pair("GLOBAL", null)
        val local = scopeKey.removePrefix(MetadataUrns.SCOPE_PREFIX)
        return when {
            local.startsWith("user:") -> Pair("USER", local.removePrefix("user:"))
            local.startsWith("team:") -> Pair("TEAM", local.removePrefix("team:"))
            local.startsWith("role:") -> Pair("ROLE", local.removePrefix("role:"))
            else                      -> Pair("CUSTOM", local)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JpaMetadataRepository::class.java)
    }
}
