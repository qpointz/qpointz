package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntityUrn

/**
 * Builds and parses **relational** metadata entity URNs of the form
 * `urn:mill/metadata/entity:<schema>[.<table>[.<column>]]`.
 *
 * Local parts that contain `:` (for example `concept:…`) are treated as non-relational and
 * [parseCatalogPath] returns an empty [CatalogPath] so schema binding skips them.
 */
object RelationalMetadataEntityUrns {

    private const val PREFIX = "urn:mill/metadata/entity:"

    /**
     * @param schema physical schema name from the provider
     * @return canonical entity URN for the schema-level entity
     */
    fun forSchema(schema: String): String {
        val s = schema.trim()
        require(s.isNotEmpty()) { "schema must not be blank" }
        return MetadataEntityUrn.canonicalize(PREFIX + s.lowercase())
    }

    /**
     * @param schema physical schema name
     * @param table physical table name
     * @return canonical entity URN for the table-level entity
     */
    fun forTable(schema: String, table: String): String {
        val s = schema.trim()
        val t = table.trim()
        require(s.isNotEmpty() && t.isNotEmpty()) { "schema and table must not be blank" }
        return MetadataEntityUrn.canonicalize("${PREFIX}${s.lowercase()}.${t.lowercase()}")
    }

    /**
     * @param schema physical schema name
     * @param table physical table name
     * @param column physical column name (dots in the name are preserved after the second dot)
     * @return canonical entity URN for the column-level entity
     */
    fun forAttribute(schema: String, table: String, column: String): String {
        val s = schema.trim()
        val t = table.trim()
        val c = column.trim()
        require(s.isNotEmpty() && t.isNotEmpty() && c.isNotEmpty()) {
            "schema, table, and column must not be blank"
        }
        return MetadataEntityUrn.canonicalize("${PREFIX}${s.lowercase()}.${t.lowercase()}.${c.lowercase()}")
    }

    /**
     * Parses a canonical entity URN into [CatalogPath]. Non-relational locals yield all-null coordinates.
     *
     * @param urn entity instance URN
     */
    fun parseCatalogPath(urn: String): CatalogPath {
        val c = runCatching { MetadataEntityUrn.canonicalize(urn) }.getOrElse { return CatalogPath(null, null, null) }
        if (!c.startsWith(PREFIX)) return CatalogPath(null, null, null)
        val local = c.removePrefix(PREFIX)
        if (local.isEmpty()) return CatalogPath(null, null, null)
        if (':' in local) return CatalogPath(null, null, null)
        val parts = local.split('.')
        return when (parts.size) {
            1 -> CatalogPath(parts[0], null, null)
            2 -> CatalogPath(parts[0], parts[1], null)
            else -> CatalogPath(parts[0], parts[1], parts.drop(2).joinToString("."))
        }
    }
}
