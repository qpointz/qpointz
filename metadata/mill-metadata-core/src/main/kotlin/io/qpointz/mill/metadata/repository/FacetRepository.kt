package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.facet.FacetAssignment

/** Persistence for facet assignment rows (`metadata_entity_facet`) — SPEC §6.2. */
interface FacetRepository {
    fun findByEntity(entityId: String): List<FacetAssignment>

    fun findByEntityAndType(entityId: String, facetTypeKey: String): List<FacetAssignment>

    fun findByEntityTypeAndScope(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String
    ): List<FacetAssignment>

    fun findByUid(uid: String): FacetAssignment?

    fun save(facet: FacetAssignment): FacetAssignment

    fun deleteByUid(uid: String): Boolean

    fun deleteByEntity(entityId: String)

    fun deleteByEntityTypeAndScope(entityId: String, facetTypeKey: String, scopeKey: String)

    /** @return number of facet assignment rows referencing the canonical facet type URN */
    fun countByFacetType(facetTypeKey: String): Int
}
