package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.WithFacets
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType

object SchemaToolHandlers {

    data class ListSchemasItem(
        val schemaName: String,
        val description: String
    )

    private fun description(item: WithFacets): String {
       return item.facets.descriptive?.description ?: ""
    }

    fun listSchemas(svc: SchemaFacetService): List<ListSchemasItem> {
        return svc.getSchemas().schemas
            .map { ListSchemasItem(it.schemaName, description(it)) }
            .toList()
    }

    data class ListTablesItem(
        val schemaName: String,
        val tableName: String,
        val description: String
    )

    fun listTables(svc: SchemaFacetService, schemaName: String): List<ListTablesItem> {
         return svc.getSchemas().schemas
            .filter { schemaName == it.schemaName }
            .flatMap { it.tables }
            .map { ListTablesItem(it.schemaName, it.tableName, description(it)) }
            .toList()
    }

    data class ListColumnsItem(
        val schemaName: String,
        val tableName: String,
        val columnName: String,
        val description: String = "",
        val nullable: DataType.Nullability = DataType.Nullability.NOT_SPECIFIED_NULL,
        val type: LogicalDataType.LogicalDataTypeId = LogicalDataType.LogicalDataTypeId.NOT_SPECIFIED_TYPE,

        )

    fun listColumns(svc: SchemaFacetService, schemaName: String, tableName: String): List<ListColumnsItem> {
        return svc.getSchemas().schemas
            .filter { schemaName == it.schemaName }
            .flatMap { it.tables }
            .filter { tableName == it.tableName }
            .flatMap { it.attributes }
            .map { ListColumnsItem(it.schemaName, it.tableName, it.attributeName,
                description = description(it),
                nullable = it.dataType.nullability,
                type = it.dataType.type.typeId
            ) }
            .toList()
    }

    data class ListRelationsItem(
        val sourceSchema: String,
        val sourceTable: String,       // EntityReference.fqn
        val sourceAttributes: List<String>,
        val targetSchema: String,
        val targetTable: String,       // EntityReference.fqn
        val targetAttributes: List<String>,
        val name: String,
        val description: String,
        val cardinality: RelationCardinality
    )

    enum class RelationDirection {
        INBOUND,
        OUTBOUND,
        BOTH
    }

    fun listRelations(svc: SchemaFacetService, schemaName: String, tableName: String, direction: RelationDirection): List<ListRelationsItem> {
        fun createRelation(relation: RelationFacet.Relation): ListRelationsItem {
            return ListRelationsItem(relation.sourceTable!!.schema!!, relation.sourceTable!!.table!!, relation.sourceAttributes,
                                     relation.targetTable!!.schema!!, relation.targetTable!!.table!!, relation.targetAttributes,
                                     relation.name!!, relation.description!!,
                                     relation.cardinality!!)
        }

        val includeOutbound = direction == RelationDirection.OUTBOUND || direction == RelationDirection.BOTH
        val includeInbound = direction == RelationDirection.INBOUND || direction == RelationDirection.BOTH

        return svc.getSchemas().schemas
            .asSequence()
            .flatMap { it.tables }
            .mapNotNull { it.facets.relation }
            .flatMap { it.relations }
            .mapNotNull {
                when {
                    includeOutbound && it.sourceTable?.schema == schemaName && it.sourceTable?.table == tableName -> createRelation(it)
                    includeInbound && it.targetTable?.schema == schemaName && it.targetTable?.table == tableName -> createRelation(it)
                    else -> null
                }
            }
            .toList()
    }

}