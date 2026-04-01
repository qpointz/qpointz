package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.source.MetadataSource
import org.slf4j.LoggerFactory

/**
 * Combines facet rows from all registered [MetadataSource] beans after each source has applied
 * [MetadataReadContext] (scopes and origin muting).
 *
 * Cardinality rules (SPEC §3b): for [FacetTargetCardinality.SINGLE], at most one effective row per
 * facet type is emitted — captured rows win over inferred when both are present; duplicate inferred
 * rows for the same type are logged and collapsed. For [FacetTargetCardinality.MULTIPLE], all
 * contributing rows are kept (sources should not emit duplicate inferred semantics for the same type).
 *
 * @param sources read-only facet contributors (typically includes [io.qpointz.mill.metadata.source.RepositoryMetadataSource])
 * @param facetCatalog supplies cardinality per facet type URN
 */
class FacetInstanceReadMerge(
    private val sources: List<MetadataSource>,
    private val facetCatalog: FacetCatalog
) {

    /**
     * Merges facet rows from every source for [entityId].
     *
     * @param entityId canonical metadata entity URN
     * @param context scope stack and optional origin allow-list
     * @return combined effective rows (stable order: sorted by [MetadataSource.originId], then source row order)
     */
    fun merge(entityId: String, context: MetadataReadContext): List<FacetInstance> {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val ordered = sources.sortedBy { it.originId }
        val flat = ordered.flatMap { src -> src.fetchForEntity(eid, context) }
        if (flat.isEmpty()) return emptyList()
        val byType = flat.groupBy { MetadataEntityUrn.canonicalize(it.facetTypeKey) }
        val out = ArrayList<FacetInstance>(byType.size)
        for ((typeKey, rows) in byType.toSortedMap()) {
            val card = facetCatalog.findDefinition(typeKey)?.targetCardinality
                ?: FacetTargetCardinality.SINGLE
            when (card) {
                FacetTargetCardinality.SINGLE -> out.addAll(mergeSingleCardinality(typeKey, eid, rows))
                FacetTargetCardinality.MULTIPLE -> {
                    warnMultipleInferred(typeKey, eid, rows)
                    out.addAll(rows)
                }
            }
        }
        return out
    }

    private fun mergeSingleCardinality(typeKey: String, entityId: String, rows: List<FacetInstance>): List<FacetInstance> {
        val inferred = rows.filter { it.origin == FacetOrigin.INFERRED }
        val captured = rows.filter { it.origin == FacetOrigin.CAPTURED }
        if (inferred.size > 1) {
            log.warn(
                "Multiple INFERRED facet rows for SINGLE type {} on entity {} — misconfiguration; using first",
                typeKey,
                entityId
            )
        }
        if (captured.size > 1) {
            log.warn(
                "Multiple CAPTURED facet rows for SINGLE type {} on entity {} — using last by source order",
                typeKey,
                entityId
            )
        }
        val chosen = when {
            captured.isNotEmpty() -> captured.last()
            inferred.isNotEmpty() -> inferred.first()
            else -> rows.last()
        }
        return listOf(chosen)
    }

    private fun warnMultipleInferred(typeKey: String, entityId: String, rows: List<FacetInstance>) {
        val inferred = rows.filter { it.origin == FacetOrigin.INFERRED }
        if (inferred.size > 1) {
            log.warn(
                "Multiple INFERRED facet rows for MULTIPLE type {} on entity {} — check metadata sources",
                typeKey,
                entityId
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FacetInstanceReadMerge::class.java)
    }
}
