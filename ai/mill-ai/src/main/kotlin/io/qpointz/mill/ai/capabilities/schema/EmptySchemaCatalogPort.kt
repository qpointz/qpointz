package io.qpointz.mill.ai.capabilities.schema

/**
 * [SchemaCatalogPort] that returns no schemas, tables, columns, or relations.
 *
 * Used as a Spring fallback when no [io.qpointz.mill.data.schema.SchemaFacetService]-backed
 * catalog is registered, so `schema` capability dependency validation still succeeds and hosts
 * can start. Tool calls yield empty results until a real catalog bean is supplied.
 */
object EmptySchemaCatalogPort : SchemaCatalogPort {

    override fun listSchemas(): List<ListSchemasItem> = emptyList()

    override fun listTables(schemaName: String): List<ListTablesItem> = emptyList()

    override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> =
        emptyList()

    override fun listRelations(
        schemaName: String,
        tableName: String,
        direction: RelationDirection,
    ): List<ListRelationsItem> = emptyList()
}
