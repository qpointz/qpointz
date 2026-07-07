package io.qpointz.mill.ai.autoconfigure.sqlquery

import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionRequest
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryResultMode
import io.qpointz.mill.data.query.engine.CallerContext
import io.qpointz.mill.data.query.engine.CreateSessionResult
import io.qpointz.mill.data.query.engine.PagedQueryPayload
import io.qpointz.mill.data.query.engine.QueryFormats
import io.qpointz.mill.data.query.engine.QueryResultExecutionService
import io.qpointz.mill.data.query.engine.SessionMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class QueryResultSqlQueryExecutionPortTest {

  private val executionService = mock<QueryResultExecutionService>()
  private val callerContext = CallerContext("alice")
  private val resolver = SqlQueryExecutionCallerContextResolver { callerContext }
  private val objectMapper = ObjectMapper()
  private val port = QueryResultSqlQueryExecutionPort(executionService, resolver, objectMapper)

  @Test
  fun shouldDescribeSql_returnSchemaWithoutRows_andDeleteSession() {
    val executionId = "exec-1"
    val page = pagedPayload(
      body = objectMapper.writeValueAsBytes(listOf(mapOf("c" to 1))),
      columnSchema = listOf(mapOf("name" to "c", "type" to "BIG_INT", "nullable" to false)),
    )
    whenever(
      executionService.create(
        context = eq(callerContext),
        sql = eq("SELECT 1"),
        defaultFormat = eq(QueryFormats.ROWS_OBJECTS),
        includeFirstPage = eq(true),
        firstPageSize = eq(1),
      ),
    ).thenReturn(
      CreateSessionResult(
        executionId = executionId,
        epoch = 0,
        metadata = SessionMetadata(executionId, 0, 1, QueryFormats.ROWS_OBJECTS),
        firstPage = page,
      ),
    )

    val result = port.describe(SqlQueryExecutionRequest(sql = "SELECT 1", maxRows = 1))

    assertThat(result.schema).extracting<String> { it.name }.containsExactly("c")
    assertThat(result.source).containsEntry("maxRows", 1)
    verify(executionService).delete(callerContext, executionId)
  }

  @Test
  fun shouldExecuteSql_pagedMode_returnRows() {
    val executionId = "exec-2"
    val rows = listOf(mapOf("country" to "DE"))
    val page = pagedPayload(
      body = objectMapper.writeValueAsBytes(rows),
      columnSchema = listOf(mapOf("name" to "country", "type" to "STRING", "nullable" to true)),
      hasNext = false,
      totalResult = 1,
    )
    whenever(
      executionService.create(
        context = any(),
        sql = any(),
        defaultFormat = any(),
        includeFirstPage = any(),
        firstPageSize = any(),
      ),
    ).thenReturn(
      CreateSessionResult(
        executionId = executionId,
        epoch = 0,
        metadata = SessionMetadata(executionId, 0, 1, QueryFormats.ROWS_OBJECTS),
        firstPage = page,
      ),
    )

    val result = port.execute(
      SqlQueryExecutionRequest(
        sql = "SELECT country FROM t",
        resultMode = SqlQueryResultMode.PAGED,
        maxRows = 10,
      ),
    )

    assertThat(result.rows).isEqualTo(rows)
    assertThat(result.resultMode).isEqualTo(SqlQueryResultMode.PAGED)
    verify(executionService).delete(callerContext, executionId)
  }

  private fun pagedPayload(
    body: ByteArray,
    columnSchema: List<Map<String, Any?>>,
    hasNext: Boolean = false,
    totalResult: Int? = null,
  ): PagedQueryPayload = PagedQueryPayload(
    epoch = 0,
    pageIndex = 0,
    pageSize = 10,
    rowCount = 1,
    totalResult = totalResult,
    hasPrevious = false,
    hasNext = hasNext,
    contentType = "application/json",
    columnSchema = columnSchema,
    body = body,
  )
}
