package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.facet.FacetAssignment

/** No-op [FacetRepository]. */
class NoOpFacetRepository : FacetRepository {
    override fun findByEntity(entityId: String): List<FacetAssignment> = emptyList()
    override fun findByEntityAndType(entityId: String, facetTypeKey: String): List<FacetAssignment> = emptyList()
    override fun findByEntityTypeAndScope(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String
    ): List<FacetAssignment> = emptyList()
    override fun findByUid(uid: String): FacetAssignment? = null
    override fun save(facet: FacetAssignment): FacetAssignment = facet
    override fun deleteByUid(uid: String): Boolean = false
    override fun deleteByEntity(entityId: String) = Unit
    override fun deleteByEntityTypeAndScope(entityId: String, facetTypeKey: String, scopeKey: String) = Unit

    override fun countByFacetType(facetTypeKey: String): Int = 0
}
