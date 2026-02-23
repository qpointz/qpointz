package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataTargetType
import io.qpointz.mill.metadata.domain.ValidationResult
import java.util.Optional

/** Catalog API for facet type registration, querying, and validation policy. */
interface FacetCatalog {
    fun register(descriptor: FacetTypeDescriptor)
    fun update(descriptor: FacetTypeDescriptor)
    fun delete(typeKey: String)

    fun get(typeKey: String): Optional<FacetTypeDescriptor>
    fun getAll(): Collection<FacetTypeDescriptor>
    fun getEnabled(): Collection<FacetTypeDescriptor>
    fun getMandatory(): Collection<FacetTypeDescriptor>
    fun getForTargetType(targetType: MetadataTargetType): Collection<FacetTypeDescriptor>

    fun isAllowed(typeKey: String): Boolean
    fun isMandatory(typeKey: String): Boolean
    fun isApplicableTo(typeKey: String, targetType: MetadataTargetType): Boolean

    fun validateFacetContent(typeKey: String, facetData: Any?): ValidationResult
    fun validateEntityFacets(entity: MetadataEntity): ValidationResult
}
