package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType

/**
 * AI-facing port for read-only schema catalog tools (`list_schemas`, `list_tables`, `list_columns`,
 * `list_relations`). Implementations live in **`mill-ai-v3-data`** (and similar adapters); **`mill-ai-v3`**
 * stays free of **`io.qpointz.mill.data.*`** types.
 *
 * Result types mirror the output schemas in `capabilities/schema.yaml`.
 */
interface SchemaCatalogPort {

    /** Returns all schemas available in the data platform. */
    fun listSchemas(): List<ListSchemasItem>

    /** Returns all tables within the given schema. */
    fun listTables(schemaName: String): List<ListTablesItem>

    /** Returns all columns for a table within a schema. */
    fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem>

    /** Returns relations for a table, filtered by [direction]. */
    fun listRelations(
        schemaName: String,
        tableName: String,
        direction: RelationDirection,
    ): List<ListRelationsItem>
}

/**
 * A single schema entry returned by [SchemaCatalogPort.listSchemas].
 *
 * @property schemaName Exact schema name to use in subsequent tool calls.
 * @property description Business description, or empty string if none.
 * @property displayName Short label from descriptive metadata, or empty string if none.
 */
data class ListSchemasItem(
    val schemaName: String,
    val description: String,
    val displayName: String = "",
)

/**
 * A single table entry returned by [SchemaCatalogPort.listTables].
 *
 * @property displayName Short label from descriptive metadata, or empty string if none.
 */
data class ListTablesItem(
    val schemaName: String,
    val tableName: String,
    val description: String,
    val displayName: String = "",
)

/**
 * A single column entry returned by [SchemaCatalogPort.listColumns].
 *
 * @property displayName Short label from descriptive metadata, or empty string if none.
 */
data class ListColumnsItem(
    val schemaName: String,
    val tableName: String,
    val columnName: String,
    val displayName: String = "",
    val description: String = "",
    val nullable: DataType.Nullability = DataType.Nullability.NOT_SPECIFIED_NULL,
    val type: LogicalDataType.LogicalDataTypeId = LogicalDataType.LogicalDataTypeId.NOT_SPECIFIED_TYPE,
)

/**
 * A single relation entry returned by [SchemaCatalogPort.listRelations].
 *
 * @property joinSql Optional join predicate text from metadata, or empty string if none.
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
    val joinSql: String = "",
)

/**
 * Filter direction for [SchemaCatalogPort.listRelations].
 */
enum class RelationDirection { INBOUND, OUTBOUND, BOTH }
