package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.facet.FacetInstance

/** Facet assignment API (SPEC §7.2). */
interface FacetService {
    fun resolve(entityId: String, context: MetadataContext): List<FacetInstance>
    fun resolveByType(entityId: String, facetTypeKey: String, context: MetadataContext): List<FacetInstance>
    fun assign(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String,
        payload: Map<String, Any?>,
        actor: String
    ): FacetInstance
    fun update(uid: String, payload: Map<String, Any?>, actor: String): FacetInstance
    fun unassign(uid: String, actor: String): Boolean
    fun unassignAll(entityId: String, facetTypeKey: String, scopeKey: String, actor: String)
}
