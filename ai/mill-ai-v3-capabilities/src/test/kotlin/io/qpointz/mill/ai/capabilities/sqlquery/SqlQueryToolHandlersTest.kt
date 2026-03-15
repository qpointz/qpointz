package io.qpointz.mill.ai.capabilities.sqlquery

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ColumnInfo
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ExecutionResult
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlExecutionService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SqlQueryToolHandlersTest {

    @Test
    fun `validateSql wraps pass result`() {
        val validator = SqlValidationService {
            ValidationResult(
                passed = true,
                normalizedSql = "select 1",
            )
        }

        val result = SqlQueryToolHandlers.validateSql(validator, "select 1", attempt = 1)

        assertEquals("sql-validation", result.artifactType)
        assertTrue(result.passed)
        assertEquals(1, result.attempt)
        assertNull(result.message)
        assertEquals("select 1", result.normalizedSql)
    }

    @Test
    fun `validateSql wraps free text failure result`() {
        val validator = SqlValidationService {
            ValidationResult(
                passed = false,
                message = "Syntax error near FROM",
            )
        }

        val result = SqlQueryToolHandlers.validateSql(validator, "select from", attempt = 2)

        assertFalse(result.passed)
        assertEquals(2, result.attempt)
        assertEquals("Syntax error near FROM", result.message)
    }

    @Test
    fun `executeSql wraps result reference without rows`() {
        val executor = SqlExecutionService { statementId, _ ->
            ExecutionResult(
                statementId = statementId,
                resultId = "res_123",
                rowCount = 10,
                columns = listOf(ColumnInfo("customer_id", "STRING")),
                truncated = false,
            )
        }

        val result = SqlQueryToolHandlers.executeSql(executor, "stmt_1", "select 1")

        assertEquals("sql-result", result.artifactType)
        assertEquals("stmt_1", result.statementId)
        assertEquals("res_123", result.resultId)
        assertEquals(10, result.rowCount)
        assertEquals(1, result.columns.size)
        assertEquals("customer_id", result.columns.first().name)
    }

    @Test
    fun `mock validator accepts basic select`() {
        val result = MockSqlValidationService().validate("select 1")
        assertTrue(result.passed)
        assertEquals("select 1", result.normalizedSql)
    }

    @Test
    fun `mock validator rejects invalid marker`() {
        val result = MockSqlValidationService().validate("select invalid from orders")
        assertFalse(result.passed)
        assertTrue(result.message!!.contains("invalid"))
    }

    @Test
    fun `mock executor can omit result id for requery case`() {
        val result = MockSqlExecutionService(includeResultId = false)
            .execute("stmt_42", "select * from orders")

        assertEquals("stmt_42", result.statementId)
        assertNull(result.resultId)
        assertTrue(result.notes.isNotEmpty())
    }
}
