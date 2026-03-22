package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataUrns

/**
 * Represents the ordered list of scope URN keys used to resolve metadata for a request.
 *
 * The context is parsed from the `?context=` query parameter, which accepts a comma-separated
 * list of prefixed slugs (e.g. `global`, `user:alice`, `team:eng`). Each slug is expanded to
 * a full scope URN via [MetadataUrns.normaliseScopePath].
 *
 * Scope precedence: last scope in the list wins when resolving merged facets.
 *
 * @property scopes ordered list of fully-qualified scope URN keys
 */
data class MetadataContext(val scopes: List<String>) {

    companion object {

        /**
         * Parses a comma-separated `context` query parameter into a [MetadataContext].
         *
         * Each comma-separated segment is normalised to a full scope URN via
         * [MetadataUrns.normaliseScopePath]. Blank segments are ignored.
         *
         * @param contextParam comma-separated scope slugs or URNs, e.g. `"global,user:alice"`
         * @return [MetadataContext] with normalised scope URNs in order
         */
        fun parse(contextParam: String?): MetadataContext {
            if (contextParam.isNullOrBlank()) return global()
            val scopes = contextParam.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { MetadataUrns.normaliseScopePath(it) }
            return MetadataContext(scopes)
        }

        /**
         * Returns the default [MetadataContext] containing only the global scope.
         *
         * @return context with [MetadataUrns.SCOPE_GLOBAL] as the single scope
         */
        fun global(): MetadataContext = MetadataContext(listOf(MetadataUrns.SCOPE_GLOBAL))
    }
}
