package io.qpointz.mill.ai.autoconfigure.sqlquery

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.ai.capabilities.sqlquery.SqlDescriptionProbeResult
import io.qpointz.mill.ai.capabilities.sqlquery.SqlExecutionProbeResult
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionException
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionRequest
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryResultMode
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQuerySchemaMapper
import io.qpointz.mill.data.query.engine.CallerContext
import io.qpointz.mill.data.query.engine.PagedQueryPayload
import io.qpointz.mill.data.query.engine.QueryFormats
import io.qpointz.mill.data.query.engine.QueryResultExecutionService
import io.qpointz.mill.data.query.engine.QuerySqlExecutionException
import kotlin.math.min

/**
 * Adapts [QueryResultExecutionService] to [SqlQueryExecutionPort] for `sql-query` tools.
 *
 * @property executionService query-result session engine
 * @property callerContextResolver maps the active security principal to [CallerContext]
 * @property objectMapper parses `rows-objects` JSON pages
 */
class QueryResultSqlQueryExecutionPort(
    private val executionService: QueryResultExecutionService,
    private val callerContextResolver: SqlQueryExecutionCallerContextResolver,
    private val objectMapper: ObjectMapper,
) : SqlQueryExecutionPort {

    override fun describe(request: SqlQueryExecutionRequest): SqlDescriptionProbeResult =
        withSession(request.sql, probePageSize = 1) { context, executionId, firstPage ->
            val schema = SqlQuerySchemaMapper.toChartFacingSchema(firstPage.columnSchema)
            SqlDescriptionProbeResult(
                sql = request.sql,
                dialectId = request.dialectId ?: DEFAULT_DIALECT_ID,
                schema = schema,
                source = mapOf("kind" to "execution", "maxRows" to 1),
            ).also {
                executionService.delete(context, executionId)
            }
        }

    override fun execute(request: SqlQueryExecutionRequest): SqlExecutionProbeResult =
        when (request.resultMode) {
            SqlQueryResultMode.PAGED -> executePaged(request)
            SqlQueryResultMode.FULL -> executeFull(request)
        }

    private fun executePaged(request: SqlQueryExecutionRequest): SqlExecutionProbeResult {
        val pageSize = request.pageSize?.coerceAtLeast(1) ?: min(request.maxRows, DEFAULT_PAGE_SIZE)
        val pageIndex = request.pageIndex.coerceAtLeast(0)
        return withSession(
            sql = request.sql,
            probePageSize = pageSize,
            includeFirstPage = pageIndex == 0,
        ) { context, executionId, firstPage ->
            val page = if (pageIndex == 0) {
                firstPage
            } else {
                executionService.getPage(
                    context,
                    executionId,
                    pageIndex,
                    pageSize,
                    QueryFormats.ROWS_OBJECTS,
                    null,
                )
            }
            try {
                val rows = parseRows(page.body)
                val schema = SqlQuerySchemaMapper.toChartFacingSchema(page.columnSchema)
                val total = page.totalResult
                val truncated = page.hasNext || (total != null && total > rows.size)
                SqlExecutionProbeResult(
                    sql = request.sql,
                    dialectId = request.dialectId ?: DEFAULT_DIALECT_ID,
                    schema = schema,
                    rows = rows,
                    resultMode = SqlQueryResultMode.PAGED,
                    rowCount = rows.size,
                    truncated = truncated,
                    hasMore = page.hasNext,
                    totalResult = page.totalResult,
                    limit = request.maxRows,
                )
            } finally {
                executionService.delete(context, executionId)
            }
        }
    }

    private fun executeFull(request: SqlQueryExecutionRequest): SqlExecutionProbeResult {
        val pageSize = min(request.maxRows, DEFAULT_PAGE_SIZE)
        return withSession(request.sql, probePageSize = pageSize, includeFirstPage = true) { context, executionId, firstPage ->
            try {
                val accumulated = mutableListOf<Map<String, Any?>>()
                var page = firstPage
                var pageIndex = 0
                while (accumulated.size < request.maxRows) {
                    val batch = parseRows(page.body)
                    val remaining = request.maxRows - accumulated.size
                    accumulated.addAll(batch.take(remaining))
                    if (accumulated.size >= request.maxRows || !page.hasNext || batch.isEmpty()) {
                        break
                    }
                    pageIndex += 1
                    page = executionService.getPage(
                        context,
                        executionId,
                        pageIndex,
                        page.pageSize,
                        QueryFormats.ROWS_OBJECTS,
                        null,
                    )
                }
                val schema = SqlQuerySchemaMapper.toChartFacingSchema(firstPage.columnSchema)
                val total = page.totalResult
                val hasMore = page.hasNext || (total != null && total > accumulated.size)
                val truncated = hasMore || accumulated.size >= request.maxRows
                SqlExecutionProbeResult(
                    sql = request.sql,
                    dialectId = request.dialectId ?: DEFAULT_DIALECT_ID,
                    schema = schema,
                    rows = accumulated,
                    resultMode = SqlQueryResultMode.FULL,
                    rowCount = accumulated.size,
                    truncated = truncated,
                    hasMore = hasMore,
                    totalResult = total,
                    limit = request.maxRows,
                )
            } finally {
                executionService.delete(context, executionId)
            }
        }
    }

    private fun <T> withSession(
        sql: String,
        probePageSize: Int,
        includeFirstPage: Boolean = true,
        block: (CallerContext, String, PagedQueryPayload) -> T,
    ): T {
        val context = callerContextResolver.resolve()
        return try {
            val created = executionService.create(
                context = context,
                sql = sql,
                defaultFormat = QueryFormats.ROWS_OBJECTS,
                includeFirstPage = includeFirstPage,
                firstPageSize = probePageSize,
            )
            val firstPage = created.firstPage
                ?: executionService.getPage(
                    context,
                    created.executionId,
                    0,
                    probePageSize,
                    QueryFormats.ROWS_OBJECTS,
                    null,
                )
            block(context, created.executionId, firstPage)
        } catch (ex: QuerySqlExecutionException) {
            throw SqlQueryExecutionException("QUERY_FAILED", ex.message ?: "Query failed", ex)
        } catch (ex: SqlQueryExecutionException) {
            throw ex
        } catch (ex: Exception) {
            throw SqlQueryExecutionException("QUERY_FAILED", ex.message ?: "Query failed", ex)
        }
    }

    private fun parseRows(body: ByteArray): List<Map<String, Any?>> {
        if (body.isEmpty()) {
            return emptyList()
        }
        return objectMapper.readValue(body, ROW_LIST_TYPE)
    }

    private companion object {
        const val DEFAULT_DIALECT_ID: String = "CALCITE"
        const val DEFAULT_PAGE_SIZE: Int = 100
        val ROW_LIST_TYPE = object : TypeReference<List<Map<String, Any?>>>() {}
    }
}
