package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.WithFacets
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType

/**
 * Pure stateless implementations of the four schema exploration tool handlers.
 *
 * Each function maps a [SchemaFacetService] query to a flat, JSON-serializable result type.
 * The result types mirror the output schemas declared in `capabilities/schema.yaml` exactly —
 * any field added here must also be reflected in the YAML output schema and vice versa.
 *
 * These handlers do not perform any caching. Each call delegates directly to
 * [SchemaFacetService.getSchemas], which owns the caching/lifecycle policy.
 */
object SchemaToolHandlers {

    /**
     * A single schema entry returned by [listSchemas].
     *
     * @property schemaName Exact schema name to use in subsequent tool calls.
     * @property description Business description from [DescriptiveFacet], or empty string if none.
     */
    data class ListSchemasItem(
        val schemaName: String,
        val description: String,
    )

    /**
     * Extracts the descriptive text from a [WithFacets] entity.
     * Returns an empty string when no [DescriptiveFacet] has been registered for the entity,
     * keeping the tool output stable regardless of metadata completeness.
     */
    private fun description(item: WithFacets): String =
        item.facets.descriptive?.description ?: ""

    /**
     * Returns all schemas available in the data platform.
     *
     * The planner should call this first in any schema-exploration or schema-authoring flow
     * to discover valid schema names before calling [listTables].
     */
    fun listSchemas(svc: SchemaFacetService): List<ListSchemasItem> =
        svc.getSchemas().schemas
            .map { ListSchemasItem(it.schemaName, description(it)) }
            .toList()

    /**
     * A single table entry returned by [listTables].
     *
     * @property schemaName Schema this table belongs to.
     * @property tableName Exact table name to use in [listColumns] or [listRelations].
     * @property description Business description from [DescriptiveFacet], or empty string if none.
     */
    data class ListTablesItem(
        val schemaName: String,
        val tableName: String,
        val description: String,
    )

    /**
     * Returns all tables within the given schema.
     *
     * Filters by [schemaName] so the result is scoped to the requested schema.
     * The planner should call [listSchemas] first to confirm the schema name exists.
     */
    fun listTables(svc: SchemaFacetService, schemaName: String): List<ListTablesItem> =
        svc.getSchemas().schemas
            .filter { schemaName == it.schemaName }
            .flatMap { it.tables }
            .map { ListTablesItem(it.schemaName, it.tableName, description(it)) }
            .toList()

    /**
     * A single column entry returned by [listColumns].
     *
     * @property schemaName Schema this column belongs to.
     * @property tableName Table this column belongs to.
     * @property columnName Exact column name.
     * @property description Business description from [DescriptiveFacet], or empty string if none.
     * @property nullable Nullability classification from the physical schema.
     * @property type Logical data type identifier (e.g. INTEGER, VARCHAR).
     */
    data class ListColumnsItem(
        val schemaName: String,
        val tableName: String,
        val columnName: String,
        val description: String = "",
        val nullable: DataType.Nullability = DataType.Nullability.NOT_SPECIFIED_NULL,
        val type: LogicalDataType.LogicalDataTypeId = LogicalDataType.LogicalDataTypeId.NOT_SPECIFIED_TYPE,
    )

    /**
     * Returns all columns for a given table within a schema.
     *
     * Filters by both [schemaName] and [tableName]. The planner should call [listTables]
     * first to confirm the table name before calling this tool.
     */
    fun listColumns(svc: SchemaFacetService, schemaName: String, tableName: String): List<ListColumnsItem> =
        svc.getSchemas().schemas
            .filter { schemaName == it.schemaName }
            .flatMap { it.tables }
            .filter { tableName == it.tableName }
            .flatMap { it.attributes }
            .map {
                ListColumnsItem(
                    it.schemaName, it.tableName, it.attributeName,
                    description = description(it),
                    nullable = it.dataType.nullability,
                    type = it.dataType.type.typeId,
                )
            }
            .toList()

    /**
     * A single relation entry returned by [listRelations].
     *
     * All id fields are the raw schema/table names from the physical schema, not canonical
     * dot-notation ids. The planner should concatenate them to build fully qualified ids when
     * passing them to capture tools.
     *
     * @property sourceSchema Schema of the source table.
     * @property sourceTable Name of the source table.
     * @property sourceAttributes Source join column names.
     * @property targetSchema Schema of the target table.
     * @property targetTable Name of the target table.
     * @property targetAttributes Target join column names.
     * @property name Relation name as declared in [RelationFacet].
     * @property description Business description of the relation.
     * @property cardinality Declared cardinality (ONE_TO_MANY, MANY_TO_ONE, ONE_TO_ONE, …).
     */
    data class ListRelationsItem(
        val sourceSchema: String,
        val sourceTable: String,
        val sourceAttributes: List<String>,
        val targetSchema: String,
        val targetTable: String,
        val targetAttributes: List<String>,
        val name: String,
        val description: String,
        val cardinality: RelationCardinality,
    )

    /**
     * Filter direction for [listRelations].
     *
     * - [OUTBOUND] — this table is the relation source (foreign-key owner side).
     * - [INBOUND]  — this table is the relation target (primary-key side).
     * - [BOTH]     — include relations in both directions.
     *
     * The direction enum is declared in the `schema.yaml` manifest as a string enum constraint
     * so the LLM is given an explicit list of valid values rather than a free-form string.
     */
    enum class RelationDirection { INBOUND, OUTBOUND, BOTH }

    /**
     * Returns relations associated with the given table, filtered by [direction].
     *
     * Scans all tables in all schemas for [RelationFacet] entries and includes those where
     * the requested table appears on the source side (OUTBOUND), target side (INBOUND), or
     * either side (BOTH). The scan is linear over all tables; for large schemas this is
     * acceptable given the read-only exploration context and typical schema sizes.
     */
    fun listRelations(
        svc: SchemaFacetService,
        schemaName: String,
        tableName: String,
        direction: RelationDirection,
    ): List<ListRelationsItem> {
        fun createRelation(relation: RelationFacet.Relation): ListRelationsItem =
            ListRelationsItem(
                relation.sourceTable!!.schema!!, relation.sourceTable!!.table!!, relation.sourceAttributes,
                relation.targetTable!!.schema!!, relation.targetTable!!.table!!, relation.targetAttributes,
                relation.name!!, relation.description!!,
                relation.cardinality!!,
            )

        val includeOutbound = direction == RelationDirection.OUTBOUND || direction == RelationDirection.BOTH
        val includeInbound  = direction == RelationDirection.INBOUND  || direction == RelationDirection.BOTH

        return svc.getSchemas().schemas
            .asSequence()
            .flatMap { it.tables }
            .mapNotNull { it.facets.relation }
            .flatMap { it.relations }
            .mapNotNull {
                when {
                    includeOutbound && it.sourceTable?.schema == schemaName && it.sourceTable?.table == tableName -> createRelation(it)
                    includeInbound  && it.targetTable?.schema == schemaName && it.targetTable?.table == tableName -> createRelation(it)
                    else -> null
                }
            }
            .toList()
    }
}
