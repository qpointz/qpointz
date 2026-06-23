package io.qpointz.mill.data.odata.resolve

import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.metadata.service.MetadataContext

/**
 * Resolves OData EDM property names to physical columns for a schema-scoped entity set.
 */
class EdmPropertyResolver(
    private val schemaFacetService: SchemaFacetService,
) {
    /**
     * @param schemaName physical schema name from the OData service root
     * @param tableName physical table name (OData entity set name)
     * @param context metadata scope for facet resolution
     * @return table metadata or null when unknown
     */
    fun resolveTable(
        schemaName: String,
        tableName: String,
        context: MetadataContext = MetadataContext.global(),
    ): SchemaTableWithFacets? =
        schemaFacetService.getTable(schemaName, tableName, context)

    /**
     * @param schemaName physical schema name
     * @param tableName OData entity set name (physical table)
     * @param propertyName EDM property name
     * @param context metadata scope
     * @return zero-based column index, or null when property is unknown
     */
    fun columnIndex(
        schemaName: String,
        tableName: String,
        propertyName: String,
        context: MetadataContext = MetadataContext.global(),
    ): Int? = resolveTable(schemaName, tableName, context)
        ?.columns
        ?.firstOrNull { it.columnName.equals(propertyName, ignoreCase = false) }
        ?.fieldIndex
}
