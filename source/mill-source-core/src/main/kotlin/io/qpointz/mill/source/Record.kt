package io.qpointz.mill.source

/**
 * A single row of data represented as a map of column names to values.
 *
 * Records are the row-oriented data unit in the source module.
 * Each key corresponds to a [SchemaField.name] and each value
 * holds the column value (or `null` for missing/null columns).
 *
 * @property values column name to value mapping
 */
data class Record(val values: Map<String, Any?>) {

    /**
     * Retrieves the value for the given column [key], or `null` if not present.
     */
    operator fun get(key: String): Any? = values[key]

    companion object {
        /**
         * Creates a [Record] from vararg name-value pairs.
         *
         * Example:
         * ```
         * val r = Record.of("id" to 1, "name" to "Alice")
         * ```
         */
        fun of(vararg pairs: Pair<String, Any?>) = Record(mapOf(*pairs))

        /**
         * Creates an empty [Record] with no columns.
         */
        fun empty() = Record(emptyMap())
    }
}
