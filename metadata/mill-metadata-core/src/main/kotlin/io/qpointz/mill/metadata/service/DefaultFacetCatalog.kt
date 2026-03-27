package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.ValidationResult
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.slf4j.LoggerFactory
import java.util.Optional

/**
 * Default [FacetCatalog] implementation backed by [FacetTypeRepository].
 *
 * @param repository       the underlying persistence store for facet type descriptors
 * @param contentValidator optional JSON-schema validator for facet payloads;
 *                         when absent, content validation is skipped
 */
class DefaultFacetCatalog(
    private val repository: FacetTypeRepository,
    private val contentValidator: FacetContentValidator? = null
) : FacetCatalog {

    override fun register(descriptor: FacetTypeDescriptor) {
        if (repository.existsByTypeKey(descriptor.typeKey)) {
            throw IllegalArgumentException("Facet type already registered: ${descriptor.typeKey}")
        }
        repository.save(descriptor)
        log.info("Registered facet type: {}", descriptor.typeKey)
    }

    override fun update(descriptor: FacetTypeDescriptor) {
        val existing = repository.findByTypeKey(descriptor.typeKey)
        if (existing.isEmpty) throw IllegalArgumentException("Facet type not found: ${descriptor.typeKey}")
        if (existing.get().mandatory && !descriptor.enabled) {
            throw IllegalArgumentException("Cannot disable mandatory facet type: ${descriptor.typeKey}")
        }
        repository.save(descriptor)
        log.info("Updated facet type: {}", descriptor.typeKey)
    }

    override fun delete(typeKey: String) {
        val existing = repository.findByTypeKey(typeKey)
        if (existing.isEmpty) return
        if (existing.get().mandatory) throw IllegalArgumentException("Cannot delete mandatory facet type: $typeKey")
        val inUse = repository.usageCount(typeKey)
        if (inUse > 0) throw IllegalArgumentException("Cannot delete facet type in use ($inUse references): $typeKey")
        repository.deleteByTypeKey(typeKey)
        log.info("Deleted facet type: {}", typeKey)
    }

    override fun get(typeKey: String): Optional<FacetTypeDescriptor> =
        repository.findByTypeKey(typeKey)

    override fun getAll(): Collection<FacetTypeDescriptor> =
        repository.findAll()

    override fun getEnabled(): Collection<FacetTypeDescriptor> =
        repository.findAll().filter { it.enabled }

    override fun getMandatory(): Collection<FacetTypeDescriptor> =
        repository.findAll().filter { it.mandatory }

    override fun getForTargetType(targetType: String): Collection<FacetTypeDescriptor> =
        repository.findAll().filter { it.isApplicableTo(targetType) }

    override fun isAllowed(typeKey: String): Boolean =
        repository.findByTypeKey(typeKey).map { it.enabled }.orElse(true)

    override fun isMandatory(typeKey: String): Boolean =
        repository.findByTypeKey(typeKey).map { it.mandatory }.orElse(false)

    override fun isApplicableTo(typeKey: String, targetType: String): Boolean =
        repository.findByTypeKey(typeKey).map { it.isApplicableTo(targetType) }.orElse(true)

    override fun validateFacetContent(typeKey: String, facetData: Any?): ValidationResult {
        val descriptorOpt = repository.findByTypeKey(typeKey)
        if (descriptorOpt.isEmpty) return ValidationResult.ok()
        val descriptor = descriptorOpt.get()
        if (!descriptor.hasContentSchema()) return ValidationResult.ok()
        if (contentValidator == null) {
            log.debug("No content validator configured, skipping schema validation for: {}", typeKey)
            return ValidationResult.ok()
        }
        return contentValidator.validate(descriptor.contentSchema!!, facetData)
    }

    @Suppress("UNCHECKED_CAST")
    override fun validateEntityFacets(entity: MetadataEntity): ValidationResult {
        val results = mutableListOf<ValidationResult>()
        val entityTargetType = toEntityTypeUrn(entity.type)

        val facets = entity.facets
        if (facets.isEmpty()) return ValidationResult.ok()

        for ((typeKey, scopedFacets) in facets) {
            val descriptorOpt = repository.findByTypeKey(typeKey)
            if (descriptorOpt.isEmpty) continue
            val descriptor = descriptorOpt.get()

            if (!descriptor.enabled) {
                results.add(ValidationResult.fail("Facet type '$typeKey' is disabled"))
                continue
            }
            if (!descriptor.isApplicableTo(entityTargetType)) {
                results.add(ValidationResult.fail("Facet type '$typeKey' is not applicable to $entityTargetType"))
                continue
            }
            if (descriptor.targetCardinality == FacetTargetCardinality.SINGLE && scopedFacets.size > 1) {
                results.add(
                    ValidationResult.fail(
                        "Facet type '$typeKey' allows a single value per entity but has ${scopedFacets.size} scoped values"
                    )
                )
                continue
            }
            if (descriptor.hasContentSchema() && contentValidator != null) {
                for ((scope, data) in scopedFacets) {
                    val scopeResult = contentValidator.validate(descriptor.contentSchema!!, data)
                    if (!scopeResult.valid) {
                        results.add(ValidationResult.fail(
                            scopeResult.errors.map { "$typeKey[$scope]: $it" }
                        ))
                    }
                }
            }
        }
        return if (results.isEmpty()) ValidationResult.ok() else ValidationResult.merge(results)
    }

    /**
     * Maps a [MetadataType] to the corresponding entity-type URN string.
     *
     * @param type the metadata entity type, or `null`
     * @return entity-type URN string from [MetadataUrns], or empty string for null
     */
    private fun toEntityTypeUrn(type: MetadataType?): String = when (type) {
        MetadataType.SCHEMA    -> MetadataUrns.ENTITY_TYPE_SCHEMA
        MetadataType.TABLE     -> MetadataUrns.ENTITY_TYPE_TABLE
        MetadataType.ATTRIBUTE -> MetadataUrns.ENTITY_TYPE_ATTRIBUTE
        MetadataType.CONCEPT   -> MetadataUrns.ENTITY_TYPE_CONCEPT
        else                   -> ""
    }

    companion object {
        private val log = LoggerFactory.getLogger(DefaultFacetCatalog::class.java)
    }
}
