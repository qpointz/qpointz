package io.qpointz.mill.data.odata.resolve

import io.qpointz.mill.data.schema.RelationFacetMaterializer
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.metadata.service.MetadataContext

/**
 * Resolves OData EDM property names to physical columns for a schema-scoped entity set.
 */
class EdmPropertyResolver @JvmOverloads constructor(
    private val schemaFacetService: SchemaFacetService,
    private val tableCache: SchemaTableCache? = null,
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
    ): SchemaTableWithFacets? {
        val key = tableCacheKey(schemaName, tableName, context)
        val cache = tableCache
        if (cache != null) {
            return cache.get(key) { schemaFacetService.getTable(schemaName, tableName, context) }
        }
        return schemaFacetService.getTable(schemaName, tableName, context)
    }

    private fun tableCacheKey(
        schemaName: String,
        tableName: String,
        context: MetadataContext,
    ): String =
        "${schemaName.lowercase()}\u0000${tableName.lowercase()}\u0000${context.scopes.joinToString(",")}\u0000${
            context.origins?.sorted()?.joinToString(",") ?: ""
        }"

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

    /**
     * @param schemaName physical schema name
     * @param entitySetName source entity set (physical table)
     * @param navigationPropertyName OData navigation property name from {@code $expand}
     * @param targetTable expanded entity set (physical table)
     * @param context metadata scope
     * @return source and target join column names from relation facet metadata
     */
    fun expandJoinColumns(
        schemaName: String,
        entitySetName: String,
        navigationPropertyName: String,
        targetTable: String,
        context: MetadataContext = MetadataContext.global(),
    ): Pair<String, String>? {
        val table = resolveTable(schemaName, entitySetName, context) ?: return null
        val schema = schemaFacetService.getSchema(schemaName, context)
        val relations = RelationFacetMaterializer.effectiveRelations(schemaName, table, schema)
        val relation = relations.firstOrNull { rel ->
            val name = RelationFacetMaterializer.navigationName(entitySetName, rel)
            name == navigationPropertyName &&
                rel.targetTable?.schema.equals(schemaName, ignoreCase = false) &&
                rel.targetTable?.table.equals(targetTable, ignoreCase = false)
        } ?: return null
        val sourceColumn = relation.sourceAttributes.firstOrNull() ?: return null
        val targetColumn = relation.targetAttributes.firstOrNull() ?: return null
        return sourceColumn to targetColumn
    }

    /**
     * @param schemaName physical schema name
     * @param entitySetName source entity set (physical table)
     * @param navigationPropertyName OData navigation property from {@code $expand}
     * @param context metadata scope
     * @return expanded target table name, or null when navigation is unknown
     */
    fun expandTargetTable(
        schemaName: String,
        entitySetName: String,
        navigationPropertyName: String,
        context: MetadataContext = MetadataContext.global(),
    ): String? {
        val table = resolveTable(schemaName, entitySetName, context) ?: return null
        val schema = schemaFacetService.getSchema(schemaName, context)
        val relations = RelationFacetMaterializer.effectiveRelations(schemaName, table, schema)
        return relations.firstOrNull { rel ->
            RelationFacetMaterializer.navigationName(entitySetName, rel) == navigationPropertyName
        }?.targetTable?.table
    }
}
