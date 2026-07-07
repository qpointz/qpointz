package io.qpointz.mill.ai.capabilities.sqlquery

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import io.qpointz.mill.sql.v2.dialect.SqlStatementNormalizer
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ColumnInfo
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ExecutionResult
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlExecutionService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ValidationResult

/**
 * Minimal mock validator for local wiring and manual validation.
 *
 * This is intentionally simple: it wraps a pass/fail decision plus free-text message in the same
 * contract expected from real downstream validation.
 */
class MockSqlValidationService : SqlValidationService {
    override fun validate(sql: String): ValidationResult {
        val trimmed = sql.trim()
        return when {
            trimmed.isBlank() -> ValidationResult(
                passed = false,
                message = "SQL statement is blank.",
            )
            !trimmed.startsWith("select", ignoreCase = true) -> ValidationResult(
                passed = false,
                message = "Mock validator currently accepts only SELECT statements.",
            )
            "invalid" in trimmed.lowercase() -> ValidationResult(
                passed = false,
                message = "Mock validator rejected SQL containing the marker 'invalid'.",
            )
            else -> ValidationResult(
                passed = true,
                message = null,
                normalizedSql = SqlStatementNormalizer.normalize(trimmed, null),
            )
        }
    }
}

/**
 * Minimal mock execution port for unit tests and local wiring without a data plane.
 */
class MockSqlQueryExecutionPort(
    private val schema: List<SqlQuerySchemaColumn> = listOf(
        SqlQuerySchemaColumn(name = "country", type = "STRING", nullable = true),
        SqlQuerySchemaColumn(name = "client_count", type = "BIG_INT", nullable = false),
    ),
    private val rows: List<Map<String, Any?>> = listOf(
        mapOf("country" to "DE", "client_count" to 42L),
    ),
    private val failDescribe: Boolean = false,
    private val failExecute: Boolean = false,
) : SqlQueryExecutionPort {

    var lastDescribeRequest: SqlQueryExecutionRequest? = null
        private set

    var lastExecuteRequest: SqlQueryExecutionRequest? = null
        private set

    override fun describe(request: SqlQueryExecutionRequest): SqlDescriptionProbeResult {
        lastDescribeRequest = request
        if (failDescribe) {
            throw SqlQueryExecutionException("QUERY_FAILED", "mock describe failure")
        }
        return SqlDescriptionProbeResult(
            sql = request.sql,
            dialectId = request.dialectId ?: "CALCITE",
            schema = schema,
            source = mapOf("kind" to "execution", "maxRows" to 1),
        )
    }

    override fun execute(request: SqlQueryExecutionRequest): SqlExecutionProbeResult {
        lastExecuteRequest = request
        if (failExecute) {
            throw SqlQueryExecutionException("QUERY_FAILED", "mock execute failure")
        }
        val limitedRows = rows.take(request.maxRows)
        val truncated = rows.size > limitedRows.size || request.resultMode == SqlQueryResultMode.FULL && rows.size >= request.maxRows
        val hasMore = rows.size > limitedRows.size
        return SqlExecutionProbeResult(
            sql = request.sql,
            dialectId = request.dialectId ?: "CALCITE",
            schema = schema,
            rows = limitedRows,
            resultMode = request.resultMode,
            rowCount = limitedRows.size,
            truncated = truncated,
            hasMore = hasMore,
            totalResult = rows.size,
            limit = request.maxRows,
        )
    }
}

/**
 * Convenience factory for tests wiring the `sql-query` capability dependency pair.
 */
fun mockSqlQueryCapabilityDependency(
    validator: SqlValidationService = MockSqlValidationService(),
    execution: SqlQueryExecutionPort = MockSqlQueryExecutionPort(),
): SqlQueryCapabilityDependency = SqlQueryCapabilityDependency(validator, execution)

/**
 * Presents [MockSqlValidationService] as a [SqlValidator] for application or Spring wiring.
 *
 * @return a [SqlValidator] backed by the same heuristics as this mock
 */
fun MockSqlValidationService.asSqlValidator(): SqlValidator =
    SqlValidator { validate(it).toSqlValidationOutcome() }

/**
 * Minimal mock execution service for **host-side** tests (not used by the `sql-query` capability).
 *
 * This simulates the result-service boundary by returning only execution metadata and an optional
 * short-lived result id. No query rows are exposed back into the agent runtime.
 */
class MockSqlExecutionService(
    private val includeResultId: Boolean = true,
) : SqlExecutionService {
    override fun execute(statementId: String, sql: String): ExecutionResult = ExecutionResult(
        statementId = statementId,
        resultId = if (includeResultId) "mock-${statementId.takeLast(8)}" else null,
        rowCount = null,
        columns = inferColumns(sql),
        truncated = null,
        notes = listOf("Mock execution service did not execute against a real engine."),
    )

    private fun inferColumns(sql: String): List<ColumnInfo> {
        val lower = sql.lowercase()
        return when {
            "count(" in lower -> listOf(ColumnInfo(name = "count", type = "BIGINT"))
            "select *" in lower -> emptyList()
            else -> listOf(ColumnInfo(name = "value", type = "UNKNOWN"))
        }
    }
}




