package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataUrns

/**
 * Read-time resolution context: ordered scopes and optional origin muting for [io.qpointz.mill.metadata.source.MetadataSource] contributions.
 *
 * Replaces the former [MetadataContext] type (same package; call sites should migrate to this name).
 *
 * @property scopes ordered scope URN keys; later entries win for last-wins merge
 * @property origins when null or empty, every origin participates; when non-empty, only [io.qpointz.mill.metadata.source.MetadataSource.originId]
 * values in this set contribute
 */
data class MetadataReadContext(
    val scopes: List<String>,
    val origins: Set<String>? = null
) {

    init {
        require(scopes.isNotEmpty()) { "MetadataReadContext must contain at least one scope" }
    }

    /**
     * @param originId contributing source id to test
     * @return false when [origins] is non-null, non-empty, and excludes [originId]
     */
    fun isOriginActive(originId: String): Boolean =
        origins.isNullOrEmpty() || originId in origins

    companion object {

        /**
         * @param scopeKey full scope URN key
         */
        fun of(scopeKey: String): MetadataReadContext = MetadataReadContext(listOf(scopeKey))

        /**
         * Parses comma-separated scope segments from the HTTP **`scope`** query parameter (preferred).
         * Legacy clients may still send **`context`** when `scope` is absent; controllers should use
         * `effectiveScope = scope ?: context` before calling this method.
         *
         * @param scopeParam comma-separated scope slugs or URNs; blank → global only
         * @param originParam optional comma-separated origin ids; blank → all origins active
         */
        fun parse(scopeParam: String?, originParam: String? = null): MetadataReadContext {
            val scopes = if (scopeParam.isNullOrBlank()) {
                listOf(MetadataUrns.SCOPE_GLOBAL)
            } else {
                scopeParam.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { MetadataUrns.normaliseScopePath(it) }
            }
            return MetadataReadContext(scopes, parseOrigins(originParam))
        }

        fun global(): MetadataReadContext = MetadataReadContext(listOf(MetadataUrns.SCOPE_GLOBAL))

        private fun parseOrigins(originParam: String?): Set<String>? {
            if (originParam.isNullOrBlank()) return null
            val parts = originParam.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isEmpty()) return null
            return parts.toSet()
        }
    }
}

/** @suppress Prefer [MetadataReadContext] in new code; Kept for incremental migration. */
typealias MetadataContext = MetadataReadContext
