package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.facet.FacetInstance

/** Persistence for facet assignment rows (`metadata_entity_facet`) — SPEC §6.2. */
interface FacetRepository {
    fun findByEntity(entityId: String): List<FacetInstance>

    fun findByEntityAndType(entityId: String, facetTypeKey: String): List<FacetInstance>

    fun findByEntityTypeAndScope(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String
    ): List<FacetInstance>

    fun findByUid(uid: String): FacetInstance?

    fun save(facet: FacetInstance): FacetInstance

    fun deleteByUid(uid: String): Boolean

    fun deleteByEntity(entityId: String)

    fun deleteByEntityTypeAndScope(entityId: String, facetTypeKey: String, scopeKey: String)

    /** @return number of facet assignment rows referencing the canonical facet type URN */
    fun countByFacetType(facetTypeKey: String): Int
}
