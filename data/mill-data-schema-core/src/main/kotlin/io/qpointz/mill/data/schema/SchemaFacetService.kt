package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.service.MetadataContext

/**
 * Aggregation boundary that merges physical schema with schema-bound metadata.
 *
 * Implementations must preserve all physical entities exposed by the underlying
 * [io.qpointz.mill.data.backend.SchemaProvider] regardless of metadata coverage.
 */
interface SchemaFacetService {
    /**
     * Returns all physical schemas merged with schema-bound metadata.
     *
     * Every schema, table, and column present in the underlying [io.qpointz.mill.data.backend.SchemaProvider]
     * is guaranteed to appear in the result regardless of metadata coverage.
     * Metadata entities that could not be matched to any physical coordinate are
     * collected in [SchemaFacetResult.unboundMetadata].
     *
     * @param context ordered scope context used for facet resolution; last scope wins when
     * multiple scopes contain values for the same facet type. Defaults to global scope.
     */
    fun getSchemas(context: MetadataContext = MetadataContext.global()): SchemaFacetResult

    /**
     * Returns the logical catalog model root merged with facets (SPEC §3f).
     *
     * @param context ordered scope context used for facet resolution
     * @return model root with stable [ModelRootWithFacets.metadataEntityId]
     */
    fun getModelRoot(context: MetadataContext = MetadataContext.global()): ModelRootWithFacets

    /**
     * Returns one physical schema merged with schema-bound metadata.
     *
     * @param schemaName physical schema name
     * @param context ordered scope context used for facet resolution
     * @return merged schema or null if physical schema does not exist
     */
    fun getSchema(schemaName: String, context: MetadataContext = MetadataContext.global()): SchemaWithFacets?

    /**
     * Returns one physical table merged with table/column metadata.
     *
     * @param schemaName physical schema name
     * @param tableName physical table name
     * @param context ordered scope context used for facet resolution
     * @return merged table or null if schema/table does not exist
     */
    fun getTable(
        schemaName: String,
        tableName: String,
        context: MetadataContext = MetadataContext.global()
    ): SchemaTableWithFacets?

    /**
     * Returns one physical column merged with column metadata.
     *
     * @param schemaName physical schema name
     * @param tableName physical table name
     * @param columnName physical column name
     * @param context ordered scope context used for facet resolution
     * @return merged column or null if schema/table/column does not exist
     */
    fun getColumn(
        schemaName: String,
        tableName: String,
        columnName: String,
        context: MetadataContext = MetadataContext.global()
    ): SchemaColumnWithFacets?
}
