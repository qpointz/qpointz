package io.qpointz.mill.ai.capabilities.sqlquery

/**
 * Maps query-result `columnSchema` entries to chart-facing schema columns.
 */
object SqlQuerySchemaMapper {

    private val CHART_FACING_KEYS = setOf("name", "type", "nullable", "nativeType")

    /**
     * @param columnSchema raw column maps from [io.qpointz.mill.data.query.engine.PagedQueryPayload.columnSchema]
     * @return trimmed schema columns for tool output
     */
    fun toChartFacingSchema(columnSchema: List<Map<String, Any?>>): List<SqlQuerySchemaColumn> =
        columnSchema.map { column ->
            SqlQuerySchemaColumn(
                name = column["name"]?.toString() ?: error("column schema entry missing name"),
                type = column["type"]?.toString() ?: "UNKNOWN",
                nullable = column["nullable"] as? Boolean,
                nativeType = column["nativeType"]?.toString(),
            )
        }

    /**
     * @param columns chart-facing columns
     * @return serializable maps for tool JSON output
     */
    fun toOutputMaps(columns: List<SqlQuerySchemaColumn>): List<Map<String, Any?>> =
        columns.map { column ->
            buildMap {
                put("name", column.name)
                put("type", column.type)
                put("nullable", column.nullable)
                column.nativeType?.let { put("nativeType", it) }
            }
        }

    /**
     * Ensures query-service presentation fields are not exposed in tool contracts.
     */
    fun assertChartFacingOnly(columnSchema: List<Map<String, Any?>>) {
        columnSchema.forEach { column ->
            column.keys.forEach { key ->
                require(key in CHART_FACING_KEYS) {
                    "Unexpected presentation field '$key' in chart-facing schema"
                }
            }
        }
    }
}
