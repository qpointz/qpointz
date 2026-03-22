package io.qpointz.mill.persistence.metadata.jpa.adapters

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Optional

/**
 * [FacetTypeRepository] adapter backed by [MetadataFacetTypeJpaRepository].
 *
 * Handles de/serialisation of [FacetTypeDescriptor.applicableTo] (a `Set<String>`) and
 * [FacetTypeDescriptor.contentSchema] (a `Map<String,Any?>`) to/from their JSON column
 * representations using Jackson.
 *
 * @param jpaRepo the underlying Spring Data JPA repository for `metadata_facet_type`
 */
class JpaFacetTypeRepository(
    private val jpaRepo: MetadataFacetTypeJpaRepository
) : FacetTypeRepository {

    private val mapper: ObjectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    /**
     * Saves a [FacetTypeDescriptor] to the database.
     *
     * If a row with the same [FacetTypeDescriptor.typeKey] already exists it is overwritten.
     *
     * @param descriptor the descriptor to persist
     */
    override fun save(descriptor: FacetTypeDescriptor) {
        val entity = toEntity(descriptor)
        jpaRepo.save(entity)
        log.info("Saved facet type: {}", descriptor.typeKey)
    }

    /**
     * Finds a [FacetTypeDescriptor] by its type key.
     *
     * @param typeKey the full Mill facet-type URN to look up
     * @return an [Optional] containing the descriptor, or empty if not found
     */
    override fun findByTypeKey(typeKey: String): Optional<FacetTypeDescriptor> =
        jpaRepo.findById(typeKey).map { toDomain(it) }

    /**
     * Returns all registered [FacetTypeDescriptor] instances.
     *
     * @return list of all descriptors in the facet type catalog
     */
    override fun findAll(): Collection<FacetTypeDescriptor> =
        jpaRepo.findAll().map { toDomain(it) }

    /**
     * Returns `true` if a facet type with the given key exists in the catalog.
     *
     * @param typeKey the full Mill facet-type URN to check
     * @return `true` if the type key is registered
     */
    override fun existsByTypeKey(typeKey: String): Boolean =
        jpaRepo.existsById(typeKey)

    /**
     * Deletes the facet type with the given key.
     *
     * No-op if the key does not exist.
     *
     * @param typeKey the full Mill facet-type URN to delete
     */
    override fun deleteByTypeKey(typeKey: String) {
        jpaRepo.deleteById(typeKey)
        log.info("Deleted facet type: {}", typeKey)
    }

    /**
     * Converts a [MetadataFacetTypeEntity] to its domain [FacetTypeDescriptor] representation.
     *
     * Deserialises [MetadataFacetTypeEntity.applicableToJson] to a `Set<String>` and
     * [MetadataFacetTypeEntity.contentSchemaJson] to a `Map<String,Any?>`.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding [FacetTypeDescriptor]
     */
    internal fun toDomain(entity: MetadataFacetTypeEntity): FacetTypeDescriptor {
        val applicableTo: Set<String>? = if (entity.applicableToJson.isBlank() || entity.applicableToJson == "[]") {
            null
        } else {
            mapper.readValue(entity.applicableToJson, object : TypeReference<Set<String>>() {})
        }
        val contentSchema: Map<String, Any?>? = entity.contentSchemaJson?.let {
            mapper.readValue(it, object : TypeReference<Map<String, Any?>>() {})
        }
        return FacetTypeDescriptor(
            typeKey = entity.typeKey,
            mandatory = entity.mandatory,
            enabled = entity.enabled,
            displayName = entity.displayName,
            description = entity.description,
            applicableTo = applicableTo,
            version = entity.version,
            contentSchema = contentSchema,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            createdBy = entity.createdBy,
            updatedBy = entity.updatedBy
        )
    }

    /**
     * Converts a [FacetTypeDescriptor] to its JPA entity representation.
     *
     * Serialises [FacetTypeDescriptor.applicableTo] to a JSON array string and
     * [FacetTypeDescriptor.contentSchema] to a JSON object string.
     *
     * @param descriptor the domain descriptor to convert
     * @return the corresponding [MetadataFacetTypeEntity]
     */
    internal fun toEntity(descriptor: FacetTypeDescriptor): MetadataFacetTypeEntity {
        val applicableToJson = if (descriptor.applicableTo.isNullOrEmpty()) "[]"
                               else mapper.writeValueAsString(descriptor.applicableTo)
        val contentSchemaJson = descriptor.contentSchema?.let { mapper.writeValueAsString(it) }
        val now = Instant.now()
        return MetadataFacetTypeEntity(
            typeKey = descriptor.typeKey,
            mandatory = descriptor.mandatory,
            enabled = descriptor.enabled,
            displayName = descriptor.displayName,
            description = descriptor.description,
            applicableToJson = applicableToJson,
            version = descriptor.version,
            contentSchemaJson = contentSchemaJson,
            createdAt = descriptor.createdAt ?: now,
            updatedAt = descriptor.updatedAt ?: now,
            createdBy = descriptor.createdBy,
            updatedBy = descriptor.updatedBy
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(JpaFacetTypeRepository::class.java)
    }
}
