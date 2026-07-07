package io.qpointz.mill.ai.capabilities.sqlquery

/**
 * Spring-free execution boundary for `sql-query.describe_sql` and `sql-query.execute_sql`.
 *
 * Default implementation adapts to [io.qpointz.mill.data.query.engine.QueryResultExecutionService]
 * in `mill-ai-autoconfigure`.
 */
interface SqlQueryExecutionPort {

    /**
     * Describes validated SQL by probing the query-result plane (MVP: one execution row, schema only).
     *
     * @param request execution request with SQL and limits
     * @return schema metadata envelope without row payload
     */
    fun describe(request: SqlQueryExecutionRequest): SqlDescriptionProbeResult

    /**
     * Executes validated SQL and returns bounded rows plus schema metadata.
     *
     * @param request execution request including result mode and row limits
     * @return execution envelope with rows and completeness metadata
     */
    fun execute(request: SqlQueryExecutionRequest): SqlExecutionProbeResult
}

/**
 * Shared input for describe and execute tool handlers.
 *
 * @property sql validated SQL text
 * @property dialectId active dialect id; defaults to CALCITE when null
 * @property resultMode paged or full accumulation semantics for execute
 * @property maxRows effective row cap after server defaults and hard limits
 * @property pageIndex zero-based page for paged mode
 * @property pageSize presentation page size for paged mode
 */
data class SqlQueryExecutionRequest(
    val sql: String,
    val dialectId: String? = null,
    val resultMode: SqlQueryResultMode = SqlQueryResultMode.PAGED,
    val maxRows: Int,
    val pageIndex: Int = 0,
    val pageSize: Int? = null,
)

/** Result mode for [SqlQueryExecutionPort.execute]. */
enum class SqlQueryResultMode {
    PAGED,
    FULL,
}

/**
 * Chart-facing column descriptor returned by execution tools.
 *
 * @property name column name
 * @property type Mill logical type name (e.g. STRING, BIG_INT)
 * @property nullable nullability when known
 * @property nativeType optional backend/JDBC label when adapter can map it
 */
data class SqlQuerySchemaColumn(
    val name: String,
    val type: String,
    val nullable: Boolean?,
    val nativeType: String? = null,
)

/**
 * Result of a schema probe ([SqlQueryExecutionPort.describe]).
 *
 * @property sql echoed SQL
 * @property dialectId resolved dialect id
 * @property schema chart-facing column schema
 * @property warnings optional non-fatal warnings
 * @property source provenance metadata
 */
data class SqlDescriptionProbeResult(
    val sql: String,
    val dialectId: String,
    val schema: List<SqlQuerySchemaColumn>,
    val warnings: List<String> = emptyList(),
    val source: Map<String, Any?> = mapOf("kind" to "execution", "maxRows" to 1),
)

/**
 * Result of bounded SQL execution ([SqlQueryExecutionPort.execute]).
 *
 * @property sql echoed SQL
 * @property dialectId resolved dialect id
 * @property schema chart-facing column schema
 * @property warnings optional non-fatal warnings
 * @property source provenance metadata
 * @property rows JSON row objects keyed by column name
 * @property resultMode echo of paged or full
 * @property rowCount number of rows in [rows]
 * @property truncated true when response is capped before complete result
 * @property hasMore true when more rows may exist beyond [rows]
 * @property totalResult total rows when known
 * @property limit effective row limit used
 */
data class SqlExecutionProbeResult(
    val sql: String,
    val dialectId: String,
    val schema: List<SqlQuerySchemaColumn>,
    val warnings: List<String> = emptyList(),
    val source: Map<String, Any?> = mapOf("kind" to "execution"),
    val rows: List<Map<String, Any?>>,
    val resultMode: SqlQueryResultMode,
    val rowCount: Int,
    val truncated: Boolean,
    val hasMore: Boolean,
    val totalResult: Int?,
    val limit: Int,
)

/**
 * Structured execution failure surfaced by handlers.
 *
 * @property code stable error code such as QUERY_FAILED
 * @property message human-readable detail
 */
class SqlQueryExecutionException(
    val code: String,
    override val message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
