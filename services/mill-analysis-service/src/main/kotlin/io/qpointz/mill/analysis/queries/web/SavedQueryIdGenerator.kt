package io.qpointz.mill.analysis.queries.web

/**
 * Generates URL-safe saved-query ids from display names.
 */
object SavedQueryIdGenerator {

    /**
     * @param name display title used as the slug source
     * @param taken ids that must not be reused
     * @return a unique catalog identifier within {@code taken}
     */
    fun generate(name: String, taken: Set<String>): String {
        var base = name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(100)
        if (base.isEmpty()) {
            base = "query"
        }
        var candidate = base
        var suffix = 2
        while (taken.contains(candidate)) {
            candidate = "$base-$suffix"
            suffix += 1
        }
        return candidate
    }
}
