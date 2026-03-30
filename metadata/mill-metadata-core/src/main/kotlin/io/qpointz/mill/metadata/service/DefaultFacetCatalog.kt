package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.FacetTypeSource
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.ValidationResult
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import java.time.Instant

/**
 * @param definitionRepository `metadata_facet_type_def`
 * @param facetTypeRepository `metadata_facet_type` runtime rows
 */
class DefaultFacetCatalog(
    private val definitionRepository: FacetTypeDefinitionRepository,
    private val facetTypeRepository: FacetTypeRepository
) : FacetCatalog {

    override fun findType(typeKey: String): FacetType? =
        facetTypeRepository.findByKey(MetadataEntityUrn.canonicalize(typeKey))

    override fun findDefinition(typeKey: String): FacetTypeDefinition? =
        definitionRepository.findByKey(MetadataEntityUrn.canonicalize(typeKey))

    override fun listDefinitions(): List<FacetTypeDefinition> = definitionRepository.findAll()

    override fun listTypes(): List<FacetType> = facetTypeRepository.findAll()

    override fun inspect(typeKey: String, payload: Map<String, Any?>): ValidationResult {
        val def = findDefinition(typeKey) ?: return ValidationResult.ok()
        if (def.contentSchema.isNullOrEmpty()) return ValidationResult.ok()
        return ValidationResult.ok()
    }

    override fun resolveCardinality(typeKey: String): FacetTargetCardinality =
        findDefinition(typeKey)?.targetCardinality ?: FacetTargetCardinality.MULTIPLE

    override fun registerDefinition(definition: FacetTypeDefinition): FacetTypeDefinition {
        val saved = definitionRepository.save(definition)
        val k = MetadataEntityUrn.canonicalize(saved.typeKey)
        val now = Instant.now()
        val existing = facetTypeRepository.findByKey(k)
        val ft = FacetType(
            typeKey = k,
            source = FacetTypeSource.DEFINED,
            definition = saved,
            createdAt = existing?.createdAt ?: now,
            createdBy = existing?.createdBy ?: saved.createdBy,
            lastModifiedAt = now,
            lastModifiedBy = saved.lastModifiedBy
        )
        facetTypeRepository.save(ft)
        return saved
    }
}
