package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.facet.FacetInstance

/**
 * Read façade for data-layer callers (SPEC §5.6): resolves facet assignments for a fixed
 * [MetadataContext] without exposing repositories.
 *
 * @param facetService facet resolution
 * @param context ordered scope URNs (caller-defined precedence)
 */
class MetadataView(
    private val facetService: FacetService,
    private val context: MetadataContext
) {
    /**
     * @param entityId canonical entity URN
     * @return effective [FacetInstance] rows after scope merge
     */
    fun resolveFacets(entityId: String): List<FacetInstance> = facetService.resolve(entityId, context)
}
