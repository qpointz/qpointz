package io.qpointz.mill.ai.valuemap

/**
 * Loads DISTINCT cell values for a physical [schema].[table].[column] (WI-182).
 *
 * Implementations typically run `SELECT DISTINCT` via the Mill data plane. [includeNull] controls whether
 * SQL NULL cells are represented as `null` entries in the returned list.
 */
fun interface ColumnDistinctValueLoader {

    /**
     * @param schema physical schema name
     * @param table physical table name
     * @param column physical column name
     * @param includeNull when true, add a `null` list element when DISTINCT yields a NULL cell
     */
    fun loadDistinctQuoted(schema: String, table: String, column: String, includeNull: Boolean): List<String?>
}
