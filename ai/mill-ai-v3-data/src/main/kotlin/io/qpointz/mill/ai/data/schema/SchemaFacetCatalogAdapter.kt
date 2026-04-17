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
import io.qpointz.mill.metadata.domain.RelationCardinality

/**
 * [SchemaCatalogPort] backed by Mill Data [SchemaFacetService] (former [SchemaToolHandlers] logic).
 */
class SchemaFacetCatalogAdapter(
    private val svc: SchemaFacetService,
) : SchemaCatalogPort {

    private fun description(item: WithFacets): String =
        item.facets.descriptive?.description ?: ""

    private fun displayName(item: WithFacets): String =
        item.facets.descriptive?.displayName ?: ""

    override fun listSchemas(): List<ListSchemasItem> =
        svc.getSchemas().schemas
            .map { ListSchemasItem(it.schemaName, description(it), displayName(it)) }

    override fun listTables(schemaName: String): List<ListTablesItem> =
        svc.getSchemas().schemas
            .filter { schemaName == it.schemaName }
            .flatMap { it.tables }
            .map { ListTablesItem(it.schemaName, it.tableName, description(it), displayName(it)) }

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
                    displayName = displayName(it),
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
        fun createRelation(relation: RelationFacet.Relation): ListRelationsItem? {
            val source = relation.sourceTable ?: return null
            val target = relation.targetTable ?: return null
            return ListRelationsItem(
                source.schema,
                source.table,
                relation.sourceAttributes,
                target.schema,
                target.table,
                relation.targetAttributes,
                relation.name ?: "",
                relation.description ?: "",
                relation.cardinality ?: RelationCardinality.MANY_TO_ONE,
                relation.joinSql ?: "",
            )
        }

        val includeOutbound = direction == RelationDirection.OUTBOUND || direction == RelationDirection.BOTH
        val includeInbound = direction == RelationDirection.INBOUND || direction == RelationDirection.BOTH

        val facetResult = svc.getSchemas()
        val fromModel = facetResult.modelRoot.facets.relation?.relations.orEmpty()
        val fromTables =
            facetResult.schemas
                .asSequence()
                .flatMap { it.tables }
                .mapNotNull { it.facets.relation?.relations }
                .flatten()
                .toList()
        val allRelations = fromModel + fromTables

        return allRelations
            .asSequence()
            .mapNotNull { relation ->
                when {
                    includeOutbound &&
                        relation.sourceTable?.schema == schemaName &&
                        relation.sourceTable?.table == tableName -> createRelation(relation)
                    includeInbound &&
                        relation.targetTable?.schema == schemaName &&
                        relation.targetTable?.table == tableName -> createRelation(relation)
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
