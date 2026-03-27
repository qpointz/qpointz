package io.qpointz.mill.persistence.metadata.jpa.adapters

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Optional

/**
 * [FacetTypeRepository] adapter backed by [MetadataFacetTypeJpaRepository] (`metadata_facet_type_def`).
 */
class JpaFacetTypeRepository(
    private val jpaRepo: MetadataFacetTypeJpaRepository,
    private val facetRepo: MetadataFacetJpaRepository
) : FacetTypeRepository {

    private val mapper: ObjectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    override fun save(descriptor: FacetTypeDescriptor) {
        val incoming = toEntity(descriptor)
        val saved = jpaRepo.findByTypeRes(descriptor.typeKey)
            .map { existing ->
                existing.mandatory = incoming.mandatory
                existing.enabled = incoming.enabled
                existing.displayName = incoming.displayName
                existing.description = incoming.description
                existing.applicableToJson = incoming.applicableToJson
                existing.version = incoming.version
                existing.contentSchemaJson = incoming.contentSchemaJson
                existing.manifestJson = incoming.manifestJson
                existing.updatedAt = incoming.updatedAt
                existing.updatedBy = incoming.updatedBy
                existing
            }.orElse(incoming)
        jpaRepo.save(saved)
        log.info("Saved facet type: {}", descriptor.typeKey)
    }

    override fun findByTypeKey(typeKey: String): Optional<FacetTypeDescriptor> =
        jpaRepo.findByTypeRes(typeKey).map { toDomain(it) }

    override fun findAll(): Collection<FacetTypeDescriptor> =
        jpaRepo.findAll().map { toDomain(it) }

    override fun existsByTypeKey(typeKey: String): Boolean =
        jpaRepo.existsByTypeRes(typeKey)

    override fun deleteByTypeKey(typeKey: String) {
        jpaRepo.findByTypeRes(typeKey).ifPresent { jpaRepo.delete(it) }
        log.info("Deleted facet type: {}", typeKey)
    }

    override fun usageCount(typeKey: String): Long =
        facetRepo.countByFacetTypeTypeRes(typeKey)

    internal fun toDomain(entity: MetadataFacetTypeEntity): FacetTypeDescriptor {
        val applicableTo: Set<String>? = if (entity.applicableToJson.isBlank() || entity.applicableToJson == "[]") {
            null
        } else {
            mapper.readValue(entity.applicableToJson, object : TypeReference<Set<String>>() {})
        }
        val contentSchema: Map<String, Any?>? = entity.contentSchemaJson?.let {
            mapper.readValue(it, object : TypeReference<Map<String, Any?>>() {})
        }
        val targetCardinality = try {
            when (mapper.readTree(entity.manifestJson).path("targetCardinality").asText("SINGLE")) {
                "MULTIPLE" -> FacetTargetCardinality.MULTIPLE
                else -> FacetTargetCardinality.SINGLE
            }
        } catch (_: Exception) {
            FacetTargetCardinality.SINGLE
        }
        return FacetTypeDescriptor(
            typeKey = entity.typeRes,
            mandatory = entity.mandatory,
            targetCardinality = targetCardinality,
            enabled = entity.enabled,
            displayName = entity.displayName,
            description = entity.description,
            applicableTo = applicableTo,
            version = entity.version,
            contentSchema = contentSchema,
            manifestJson = entity.manifestJson,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            createdBy = entity.createdBy,
            updatedBy = entity.updatedBy
        )
    }

    internal fun toEntity(descriptor: FacetTypeDescriptor): MetadataFacetTypeEntity {
        val applicableToJson = if (descriptor.applicableTo.isNullOrEmpty()) "[]"
        else mapper.writeValueAsString(descriptor.applicableTo)
        val contentSchemaJson = descriptor.contentSchema?.let { mapper.writeValueAsString(it) }
        val now = Instant.now()
        return MetadataFacetTypeEntity(
            facetTypeDefId = 0L,
            typeRes = descriptor.typeKey,
            mandatory = descriptor.mandatory,
            enabled = descriptor.enabled,
            displayName = descriptor.displayName,
            description = descriptor.description,
            applicableToJson = applicableToJson,
            version = descriptor.version,
            contentSchemaJson = contentSchemaJson,
            manifestJson = descriptor.manifestJson ?: "{}",
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
