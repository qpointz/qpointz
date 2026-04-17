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
 * Case-insensitive alignment between **metadata catalog coordinates** ([CatalogPath] segments from
 * canonical lowercase entity URNs) and **physical** names returned by [SchemaProvider] (Calcite /
 * JDBC may use different casing).
 *
 * Used by [SchemaFacetServiceImpl] for column lookup and by value-mapping refresh to avoid marking
 * attributes STALE when only casing differs, and to pass resolved identifiers to SQL.
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
     * Resolves [path] to the actual schema, table, and column names exposed by [provider].
     *
     * Returns null when [path] is incomplete or nothing matches (same conditions as
     * [physicalColumnPresent] false).
     */
    fun resolvePhysicalIdentifiers(provider: SchemaProvider, path: CatalogPath): PhysicalCatalogIdentifiers? {
        val schemaHint = path.schema ?: return null
        val tableHint = path.table ?: return null
        val columnHint = path.column ?: return null

        val schemaName =
            provider.getSchemaNames().firstOrNull { it.equals(schemaHint, ignoreCase = true) }
                ?: return null

        val table: Table =
            provider.getTable(schemaName, tableHint)
                ?: provider.getSchema(schemaName).tablesList
                    .firstOrNull { it.name.equals(tableHint, ignoreCase = true) }
                ?: return null

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
