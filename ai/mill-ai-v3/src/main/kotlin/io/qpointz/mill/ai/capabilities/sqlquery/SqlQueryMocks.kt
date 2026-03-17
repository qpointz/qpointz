package io.qpointz.mill.ai.capabilities.sqlquery

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
                normalizedSql = trimmed,
            )
        }
    }
}

/**
 * Minimal mock execution service.
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
