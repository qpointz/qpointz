package io.qpointz.mill.persistence.metadata.jpa.adapters

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.MetadataFacetInstanceRow
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeInstEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeInstJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * [MetadataRepository] adapter backed by JPA Spring Data repositories.
 *
 * Facet payloads are stored as normalised rows in `metadata_facet`. Domain entity and scope
 * keys remain string FQDNs/URNs (`entity_res`, `scope_res`); this adapter resolves them to
 * surrogate FKs.
 *
 * @param entityRepo        JPA repository for `metadata_entity`
 * @param facetRepo         JPA repository for `metadata_facet`
 * @param facetTypeInstRepo JPA repository for runtime facet types
 * @param facetTypeDefRepo  JPA repository for canonical facet type definitions
 * @param scopeRepo         JPA repository for `metadata_scope`
 */
@Transactional
class JpaMetadataRepository(
    private val entityRepo: MetadataEntityJpaRepository,
    private val facetRepo: MetadataFacetJpaRepository,
    private val facetTypeInstRepo: MetadataFacetTypeInstJpaRepository,
    private val facetTypeDefRepo: MetadataFacetTypeJpaRepository,
    private val scopeRepo: MetadataScopeJpaRepository
) : MetadataRepository {

    private val mapper: ObjectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    @Transactional
    override fun save(entity: MetadataEntity) {
        val id = entity.id ?: throw IllegalArgumentException("MetadataEntity.id must not be null")
        val now = Instant.now()

        val record = if (entityRepo.existsByEntityRes(id)) {
            entityRepo.findByEntityRes(id).get().also { r ->
                r.entityType = entity.type?.name ?: r.entityType
                r.schemaName = entity.schemaName
                r.tableName = entity.tableName
                r.attributeName = entity.attributeName
                r.updatedAt = now
                r.updatedBy = entity.updatedBy
            }
        } else {
            MetadataEntityRecord(
                entityId = 0L,
                entityRes = id,
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
        val persisted = entityRepo.save(record)
        val persistedEntity = entityRepo.findById(persisted.entityId).orElseThrow()

        // Snapshot existing rows so we can preserve facet_uid across delete+reinsert. Then remove all
        // rows for this entity so facet types/scopes dropped from the domain are actually deleted from
        // the DB (the loop below only reinserts keys still present on [entity.facets]).
        val existingByTypeScope: Map<Pair<String, String>, List<MetadataFacetEntity>> =
            facetRepo.findByEntityEntityRes(id)
                .groupBy { Pair(it.facetType.typeRes, it.scope.scopeRes) }
                .mapValues { (_, rows) -> rows.sortedBy { it.facetId } }
        facetRepo.deleteByEntityEntityRes(id)

        for ((facetType, scopeMap) in entity.facets) {
            for ((scopeKey, payload) in scopeMap) {
                val scopeEntity = resolveOrCreateScope(scopeKey)
                val typeInst = resolveOrCreateFacetTypeInstance(facetType, now, entity.updatedBy)
                val existingOrdered = existingByTypeScope[Pair(facetType, scopeEntity.scopeRes)].orEmpty()
                val items = flattenPayloadForCardinality(facetType, payload)
                items.forEachIndexed { idx, payloadItem ->
                    val payloadJson = if (payloadItem == null) "null" else mapper.writeValueAsString(payloadItem)
                    val facetUid = existingOrdered.getOrNull(idx)?.facetUid ?: UUID.randomUUID().toString()
                    facetRepo.save(
                        MetadataFacetEntity(
                            entity = persistedEntity,
                            scope = scopeEntity,
                            facetType = typeInst,
                            payloadJson = payloadJson,
                            facetUid = facetUid,
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

    override fun findById(id: String): Optional<MetadataEntity> {
        val record = entityRepo.findByEntityRes(id).orElse(null) ?: return Optional.empty()
        val facetRows = facetRepo.findByEntityEntityRes(record.entityRes)
        return Optional.of(toDomain(record, facetRows))
    }

    override fun findByLocation(schema: String?, table: String?, attribute: String?): Optional<MetadataEntity> {
        val record = entityRepo
            .findBySchemaNameAndTableNameAndAttributeName(schema, table, attribute)
            .orElse(null) ?: return Optional.empty()
        val facetRows = facetRepo.findByEntityEntityRes(record.entityRes)
        return Optional.of(toDomain(record, facetRows))
    }

    override fun findByType(type: MetadataType): List<MetadataEntity> {
        return entityRepo.findByEntityType(type.name).map { record ->
            val facetRows = facetRepo.findByEntityEntityRes(record.entityRes)
            toDomain(record, facetRows)
        }
    }

    override fun findAll(): List<MetadataEntity> {
        return entityRepo.findAll().map { record ->
            val facetRows = facetRepo.findByEntityEntityRes(record.entityRes)
            toDomain(record, facetRows)
        }
    }

    @Transactional
    override fun deleteById(id: String) {
        entityRepo.deleteByEntityRes(id)
        log.info("Deleted entity: {}", id)
    }

    override fun existsById(id: String): Boolean = entityRepo.existsByEntityRes(id)

    @Transactional
    override fun deleteAll() {
        entityRepo.deleteAll()
        log.info("Deleted all metadata entities")
    }

    override fun listFacetInstanceRows(entityRes: String): List<MetadataFacetInstanceRow> =
        facetRepo.findByEntityEntityRes(entityRes)
            .sortedBy { it.facetId }
            .map { toFacetInstanceRow(it) }

    override fun findFacetInstanceRow(entityRes: String, facetUid: String): MetadataFacetInstanceRow? {
        val row = facetRepo.findByFacetUid(facetUid).orElse(null) ?: return null
        if (row.entity.entityRes != entityRes) return null
        return toFacetInstanceRow(row)
    }

    override fun deleteFacetRowByUid(entityRes: String, facetUid: String): Boolean {
        val row = facetRepo.findByFacetUid(facetUid).orElse(null) ?: return false
        if (row.entity.entityRes != entityRes) return false
        facetRepo.delete(row)
        touchEntityRecord(entityRes)
        return true
    }

    override fun countFacetInstancesAtScope(entityRes: String, facetTypeUrn: String, scopeUrn: String): Int =
        facetRepo.countByEntityResAndFacetTypeResAndScopeRes(entityRes, facetTypeUrn, scopeUrn).toInt()

    override fun resolveFacetTargetCardinality(facetTypeUrn: String): FacetTargetCardinality =
        resolveTargetCardinality(facetTypeUrn)

    private fun touchEntityRecord(entityRes: String) {
        entityRepo.findByEntityRes(entityRes).ifPresent { record ->
            record.updatedAt = Instant.now()
            entityRepo.save(record)
        }
    }

    private fun toFacetInstanceRow(row: MetadataFacetEntity): MetadataFacetInstanceRow {
        val payload: Any? = if (row.payloadJson == "null") null
        else mapper.readValue(row.payloadJson, object : TypeReference<Any?>() {})
        return MetadataFacetInstanceRow(
            facetTypeKey = row.facetType.typeRes,
            scopeKey = row.scope.scopeRes,
            facetUid = row.facetUid,
            sortKey = row.facetId,
            payload = payload
        )
    }

    internal fun toDomain(
        record: MetadataEntityRecord,
        facetRows: List<MetadataFacetEntity>
    ): MetadataEntity {
        val grouped = mutableMapOf<Pair<String, String>, MutableList<Any?>>()
        for (row in facetRows) {
            val scopeKey = row.scope.scopeRes
            val payload: Any? = if (row.payloadJson == "null") null
            else mapper.readValue(row.payloadJson, object : TypeReference<Any?>() {})
            val facetTypeKey = row.facetType.typeRes
            grouped.getOrPut(Pair(facetTypeKey, scopeKey)) { mutableListOf() }.add(payload)
        }

        val facets = mutableMapOf<String, MutableMap<String, Any?>>()
        for ((key, values) in grouped) {
            val (facetTypeKey, scopeKey) = key
            val cardinality = resolveTargetCardinality(facetTypeKey)
            val payloadValue: Any? = if (cardinality == FacetTargetCardinality.MULTIPLE) values else values.lastOrNull()
            facets.getOrPut(facetTypeKey) { mutableMapOf() }[scopeKey] = payloadValue
        }
        return MetadataEntity(
            id = record.entityRes,
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

    internal fun resolveOrCreateScope(scopeKey: String): MetadataScopeEntity {
        val existing = scopeRepo.findByScopeRes(scopeKey)
        if (existing.isPresent) return existing.get()

        val (scopeType, referenceId) = parseScopeKey(scopeKey)
        val scope = MetadataScopeEntity(
            scopeId = 0L,
            scopeRes = scopeKey,
            scopeType = scopeType,
            referenceId = referenceId,
            displayName = null,
            ownerId = null,
            visibility = "PUBLIC",
            createdAt = Instant.now()
        )
        return scopeRepo.save(scope)
    }

    private fun resolveOrCreateFacetTypeInstance(typeKey: String, now: Instant, actor: String?): MetadataFacetTypeInstEntity {
        val existing = facetTypeInstRepo.findByTypeRes(typeKey)
        if (existing.isPresent) return existing.get()
        val def = facetTypeDefRepo.findByTypeRes(typeKey).orElse(null)
        val local = typeKey.substringAfterLast(':', typeKey)
        val slug = local.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { local.lowercase() }
        return facetTypeInstRepo.save(
            MetadataFacetTypeInstEntity(
                facetTypeId = 0L,
                typeRes = typeKey,
                slug = slug,
                displayName = def?.displayName,
                description = def?.description,
                source = if (def != null) "DEFINED" else "OBSERVED",
                facetTypeDef = def,
                createdAt = now,
                updatedAt = now,
                createdBy = actor,
                updatedBy = actor
            )
        )
    }

    internal fun resolveTargetCardinality(typeKey: String): FacetTargetCardinality {
        val def: MetadataFacetTypeEntity = facetTypeDefRepo.findByTypeRes(typeKey).orElse(null) ?: return FacetTargetCardinality.SINGLE
        return try {
            when (mapper.readTree(def.manifestJson).path("targetCardinality").asText("SINGLE")) {
                "MULTIPLE" -> FacetTargetCardinality.MULTIPLE
                else -> FacetTargetCardinality.SINGLE
            }
        } catch (_: Exception) {
            FacetTargetCardinality.SINGLE
        }
    }

    private fun flattenPayloadForCardinality(typeKey: String, payload: Any?): List<Any?> {
        return if (resolveTargetCardinality(typeKey) == FacetTargetCardinality.MULTIPLE) {
            (payload as? List<*>)?.toList() ?: listOf(payload)
        } else {
            listOf(payload)
        }
    }

    private fun parseScopeKey(scopeKey: String): Pair<String, String?> {
        if (scopeKey == MetadataUrns.SCOPE_GLOBAL) return Pair("GLOBAL", null)
        val local = scopeKey.removePrefix(MetadataUrns.SCOPE_PREFIX)
        return when {
            local.startsWith("user:") -> Pair("USER", local.removePrefix("user:"))
            local.startsWith("team:") -> Pair("TEAM", local.removePrefix("team:"))
            local.startsWith("role:") -> Pair("ROLE", local.removePrefix("role:"))
            else -> Pair("CUSTOM", local)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JpaMetadataRepository::class.java)
    }
}
