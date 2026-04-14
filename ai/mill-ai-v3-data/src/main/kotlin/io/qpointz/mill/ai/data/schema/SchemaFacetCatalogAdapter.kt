package io.qpointz.mill.ai.data.schema

import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.WithFacets
import io.qpointz.mill.data.schema.facet.RelationFacet

/**
 * [SchemaCatalogPort] backed by Mill Data [SchemaFacetService] (former [SchemaToolHandlers] logic).
 */
class SchemaFacetCatalogAdapter(
    private val svc: SchemaFacetService,
) : SchemaCatalogPort {

    private fun description(item: WithFacets): String =
        item.facets.descriptive?.description ?: ""

    override fun listSchemas(): List<ListSchemasItem> =
        svc.getSchemas().schemas
            .map { ListSchemasItem(it.schemaName, description(it)) }

    override fun listTables(schemaName: String): List<ListTablesItem> =
        svc.getSchemas().schemas
            .filter { schemaName == it.schemaName }
            .flatMap { it.tables }
            .map { ListTablesItem(it.schemaName, it.tableName, description(it)) }

    override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> =
        svc.getSchemas().schemas
            .filter { schemaName == it.schemaName }
            .flatMap { it.tables }
            .filter { tableName == it.tableName }
            .flatMap { it.columns }
            .map {
                ListColumnsItem(
                    it.schemaName,
                    it.tableName,
                    it.columnName,
                    description = description(it),
                    nullable = it.dataType.nullability,
                    type = it.dataType.type.typeId,
                )
            }

    override fun listRelations(
        schemaName: String,
        tableName: String,
        direction: RelationDirection,
    ): List<ListRelationsItem> {
        fun createRelation(relation: RelationFacet.Relation): ListRelationsItem =
            ListRelationsItem(
                relation.sourceTable!!.schema!!,
                relation.sourceTable!!.table!!,
                relation.sourceAttributes,
                relation.targetTable!!.schema!!,
                relation.targetTable!!.table!!,
                relation.targetAttributes,
                relation.name!!,
                relation.description!!,
                relation.cardinality!!,
            )

        val includeOutbound = direction == RelationDirection.OUTBOUND || direction == RelationDirection.BOTH
        val includeInbound = direction == RelationDirection.INBOUND || direction == RelationDirection.BOTH

        return svc.getSchemas().schemas
            .asSequence()
            .flatMap { it.tables }
            .mapNotNull { it.facets.relation }
            .flatMap { it.relations }
            .mapNotNull {
                when {
                    includeOutbound &&
                        it.sourceTable?.schema == schemaName &&
                        it.sourceTable?.table == tableName -> createRelation(it)
                    includeInbound &&
                        it.targetTable?.schema == schemaName &&
                        it.targetTable?.table == tableName -> createRelation(it)
                    else -> null
                }
            }
            .toList()
    }
}

/**
 * Adapts this [SchemaFacetService] to [SchemaCatalogPort] for non-Spring call sites (CLI, tests).
 */
fun SchemaFacetService.asSchemaCatalogPort(): SchemaCatalogPort = SchemaFacetCatalogAdapter(this)
