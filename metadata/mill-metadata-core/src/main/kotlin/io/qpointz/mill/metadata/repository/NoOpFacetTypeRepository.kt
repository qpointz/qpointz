package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetType

/** No-op runtime [FacetTypeRepository]. */
class NoOpFacetTypeRepository : FacetTypeRepository {
    override fun findByKey(typeKey: String): FacetType? = null
    override fun findAll(): List<FacetType> = emptyList()
    override fun findDefined(): List<FacetType> = emptyList()
    override fun findObserved(): List<FacetType> = emptyList()
    override fun save(facetType: FacetType): FacetType = facetType
    override fun delete(typeKey: String) = Unit
}
