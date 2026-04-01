package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.facet.FacetAssignment

/**
 * Read-only projection of facet assignment persistence (`metadata_entity_facet`) — SPEC §6.2.
 *
 * [io.qpointz.mill.metadata.source.RepositoryMetadataSource] and merge-trace endpoints should depend
 * on this type instead of [FacetRepository] when they do not mutate rows.
 */
interface FacetReadSide {

    fun findByEntity(entityId: String): List<FacetAssignment>

    fun findByEntityAndType(entityId: String, facetTypeKey: String): List<FacetAssignment>

    fun findByEntityTypeAndScope(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String,
    ): List<FacetAssignment>

    fun findByUid(uid: String): FacetAssignment?

    /** @return number of facet assignment rows referencing the canonical facet type URN */
    fun countByFacetType(facetTypeKey: String): Int
}
