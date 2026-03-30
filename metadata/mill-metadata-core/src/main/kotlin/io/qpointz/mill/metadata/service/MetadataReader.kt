package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction

/**
 * Repository-agnostic merge of stored facet assignments for a single entity (SPEC §5.6).
 *
 * Loads raw rows from persistence; applies caller-ordered [MetadataContext] scopes with
 * last-wins semantics and interprets [MergeAction] per facet-type cardinality from [FacetCatalog].
 *
 * @param facetCatalog supplies [FacetTargetCardinality] for each facet type key
 */
class MetadataReader(
    private val facetCatalog: FacetCatalog
) {

    /**
     * Computes the effective facet list for one entity from all persisted assignments.
     *
     * @param allAssignments every [FacetInstance] row for the entity (any scope)
     * @param context ordered scope stack; later scopes override earlier ones per type
     * @return merged rows suitable for schema / API projection
     */
    fun resolveEffective(
        allAssignments: List<FacetInstance>,
        context: MetadataContext
    ): List<FacetInstance> {
        val byType = allAssignments.groupBy { it.facetTypeKey }
        val result = mutableListOf<FacetInstance>()
        for ((typeKey, rows) in byType) {
            val card = facetCatalog.resolveCardinality(typeKey)
            var effective = emptyList<FacetInstance>()
            for (scope in context.scopes) {
                val cs = MetadataEntityUrn.canonicalize(scope)
                val atScope = rows.filter { it.scopeKey == cs }
                if (atScope.isEmpty()) continue
                if (atScope.any { it.mergeAction == MergeAction.TOMBSTONE }) {
                    effective = emptyList()
                    break
                }
                val contributing = atScope.filter { it.mergeAction == MergeAction.SET }
                if (contributing.isEmpty()) continue
                effective = if (card == FacetTargetCardinality.SINGLE) {
                    listOf(contributing.last())
                } else {
                    contributing
                }
            }
            result.addAll(effective)
        }
        return result
    }
}
