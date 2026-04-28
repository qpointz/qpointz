package io.qpointz.mill.metadata.service

import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns

/**
 * Parses the `scope` query for metadata export: filters **facet assignment rows** only
 * (not scope rows or facet type definition documents).
 */
internal sealed class MetadataExportFacetScopeFilter {

    /** Include every facet row for every entity. */
    data object AllScopes : MetadataExportFacetScopeFilter()

    /** Include facet rows whose canonical `scopeUrn` is in [scopeUrns]. */
    data class Union(val scopeUrns: Set<String>) : MetadataExportFacetScopeFilter()

    companion object {
        /**
         * @param scopeQuery raw `scope` query value; `null` or blank → global scope only;
         * `all` / `*` (case-insensitive) → no filter; comma-separated → union of normalised URNs
         */
        fun parse(scopeQuery: String?): MetadataExportFacetScopeFilter {
            val raw = scopeQuery?.trim().orEmpty()
            if (raw.isEmpty()) {
                return Union(setOf(MetadataEntityUrn.canonicalize(MetadataUrns.SCOPE_GLOBAL)))
            }
            if (raw.equals("all", ignoreCase = true) || raw == "*") {
                return AllScopes
            }
            val segments = raw.split(',').map { it.trim() }
            if (segments.any { it.isEmpty() }) {
                throw MillStatuses.badRequestRuntime("scope query must not contain empty segments")
            }
            val urns = linkedSetOf<String>()
            for (seg in segments) {
                val urn = try {
                    MetadataUrns.normaliseScopePath(seg)
                } catch (_: IllegalArgumentException) {
                    throw MillStatuses.badRequestRuntime("Invalid scope token: $seg")
                }
                urns += MetadataEntityUrn.canonicalize(urn)
            }
            return Union(urns)
        }
    }
}
