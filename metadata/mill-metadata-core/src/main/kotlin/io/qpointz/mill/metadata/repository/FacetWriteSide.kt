package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.facet.FacetAssignment

/**
 * Write projection of facet assignment persistence (`metadata_entity_facet`) — SPEC §6.2.
 */
interface FacetWriteSide {

    fun save(facet: FacetAssignment): FacetAssignment

    fun deleteByUid(uid: String): Boolean

    fun deleteByEntity(entityId: String)

    fun deleteByEntityTypeAndScope(entityId: String, facetTypeKey: String, scopeKey: String)
}
