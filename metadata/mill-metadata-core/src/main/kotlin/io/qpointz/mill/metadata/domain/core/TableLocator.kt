package io.qpointz.mill.metadata.domain.core

/**
 * Physical schema + table pair used inside relation facet payloads (replaces coordinate-heavy
 * entity references).
 *
 * @property schema physical schema name
 * @property table physical table name
 */
data class TableLocator(
    val schema: String,
    val table: String
) {
    /**
     * @param schema physical schema name
     * @param table physical table name
     * @param attribute unused; reserved for API symmetry with legacy matchers
     */
    fun matches(schema: String, table: String, attribute: String?): Boolean =
        this.schema.equals(schema, ignoreCase = true) && this.table.equals(table, ignoreCase = true)
}
