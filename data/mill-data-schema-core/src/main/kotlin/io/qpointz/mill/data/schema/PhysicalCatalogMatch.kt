package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.metadata.CatalogPath
import io.qpointz.mill.proto.Table

/**
 * Physical schema / table / column names as returned by [SchemaProvider] (true casing for SQL and
 * DISTINCT loaders).
 *
 * @property schema name from [SchemaProvider.getSchemaNames] / sub-schema resolution
 * @property table [Table.name] from the resolved physical table
 * @property column [io.qpointz.mill.proto.Field.name] for the matched field
 */
data class PhysicalCatalogIdentifiers(
    val schema: String,
    val table: String,
    val column: String,
)

/**
 * Case-insensitive alignment between **hierarchical metadata coordinates** ([CatalogPath] segments
 * from model entity URNs and `/model/...` routes — case-insensitive by design) and **physical**
 * names returned by [SchemaProvider] (Calcite / JDBC may use different casing).
 *
 * Used by [SchemaFacetServiceImpl] for schema/table/column lookup and by value-mapping refresh to
 * avoid marking attributes STALE when only casing differs, and to pass resolved identifiers to SQL.
 */
object PhysicalCatalogMatch {

    /**
     * Two catalog name segments refer to the same physical object (null-safe; case-insensitive).
     */
    fun coordinateEquals(left: String?, right: String?): Boolean {
        if (left == null || right == null) return left == right
        return left.equals(right, ignoreCase = true)
    }

    /**
     * Resolves a metadata schema coordinate to the physical schema name on [provider].
     *
     * @param schemaHint hierarchical metadata schema segment (any casing)
     * @return physical schema name, or null when no schema matches
     */
    fun resolvePhysicalSchema(provider: SchemaProvider, schemaHint: String): String? {
        val hint = schemaHint.trim()
        if (hint.isEmpty()) return null
        return provider.getSchemaNames().firstOrNull { it.equals(hint, ignoreCase = true) }
    }

    /**
     * Resolves metadata schema/table coordinates to the physical table on [provider].
     *
     * Prefers a direct [SchemaProvider.getTable] hit with the resolved physical schema and the
     * caller's table hint, then falls back to a case-insensitive scan of the schema's table list.
     *
     * @param schemaHint hierarchical metadata schema segment (any casing)
     * @param tableHint hierarchical metadata table segment (any casing)
     * @return physical schema name and table snapshot, or null when either segment does not match
     */
    fun resolvePhysicalTable(
        provider: SchemaProvider,
        schemaHint: String,
        tableHint: String,
    ): Pair<String, Table>? {
        val schemaName = resolvePhysicalSchema(provider, schemaHint) ?: return null
        val tableHintTrimmed = tableHint.trim()
        if (tableHintTrimmed.isEmpty()) return null

        val table: Table =
            provider.getTable(schemaName, tableHintTrimmed)
                ?: provider.getSchema(schemaName).tablesList
                    .firstOrNull { it.name.equals(tableHintTrimmed, ignoreCase = true) }
                ?: return null

        return schemaName to table
    }

    /**
     * Resolves [path] to the actual schema, table, and column names exposed by [provider].
     *
     * Returns null when [path] is incomplete or nothing matches (same conditions as
     * [physicalColumnPresent] false).
     */
    fun resolvePhysicalIdentifiers(provider: SchemaProvider, path: CatalogPath): PhysicalCatalogIdentifiers? {
        val schemaHint = path.schema ?: return null
        val tableHint = path.table ?: return null
        val columnHint = path.column?.trim().orEmpty()
        if (columnHint.isEmpty()) return null

        val (schemaName, table) = resolvePhysicalTable(provider, schemaHint, tableHint) ?: return null

        val field =
            table.fieldsList.firstOrNull { it.name.equals(columnHint, ignoreCase = true) }
                ?: return null

        return PhysicalCatalogIdentifiers(
            schema = schemaName,
            table = table.name,
            column = field.name,
        )
    }

    /**
     * True when [path] has schema, table, and column segments and they resolve to an existing field
     * on [provider].
     *
     * @param path relational coordinates (typically lowercase from [io.qpointz.mill.data.metadata.ModelEntityUrn])
     */
    fun physicalColumnPresent(provider: SchemaProvider, path: CatalogPath): Boolean =
        resolvePhysicalIdentifiers(provider, path) != null
}
