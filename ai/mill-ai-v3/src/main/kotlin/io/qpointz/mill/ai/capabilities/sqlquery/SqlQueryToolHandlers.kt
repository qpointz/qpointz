package io.qpointz.mill.ai.capabilities.sqlquery

/**
 * Pure stateless implementations of SQL query capability tools.
 *
 * These handlers intentionally sit below the capability wiring layer so they can be unit-tested
 * without capability registry, manifest loading, or Spring involvement.
 */
object SqlQueryToolHandlers {

    /**
     * Structured validation wrapper used by the runtime even when the downstream SQL engine
     * provides only pass/fail plus a free-text error message.
     */
    data class SqlValidationArtifact(
        val artifactType: String = "sql-validation",
        val passed: Boolean,
        val attempt: Int,
        val message: String? = null,
        val normalizedSql: String? = null,
    )

    /**
     * Structured execution result reference returned to the runtime/client.
     *
     * Query rows are intentionally not embedded here. The client is expected to use [resultId]
     * to fetch data later, or to requery when the result is non-durable and [resultId] is absent.
     */
    data class SqlResultReferenceArtifact(
        val artifactType: String = "sql-result",
        val statementId: String,
        val resultId: String? = null,
        val rowCount: Long? = null,
        val columns: List<ColumnInfo> = emptyList(),
        val truncated: Boolean? = null,
        val notes: List<String> = emptyList(),
    )

    data class ColumnInfo(
        val name: String,
        val type: String,
    )

    /**
     * Minimal validator boundary expected by the capability.
     */
    fun interface SqlValidationService {
        fun validate(sql: String): ValidationResult
    }

    data class ValidationResult(
        val passed: Boolean,
        val message: String? = null,
        val normalizedSql: String? = null,
    )

    /**
     * Execution boundary expected by the capability.
     *
     * Implementations may delegate to a result service that persists query results and returns
     * a short-lived result id, but that persistence layer is outside this capability.
     */
    fun interface SqlExecutionService {
        fun execute(statementId: String, sql: String): ExecutionResult
    }

    data class ExecutionResult(
        val statementId: String,
        val resultId: String? = null,
        val rowCount: Long? = null,
        val columns: List<ColumnInfo> = emptyList(),
        val truncated: Boolean? = null,
        val notes: List<String> = emptyList(),
    )

    fun validateSql(
        validator: SqlValidationService,
        sql: String,
        attempt: Int,
    ): SqlValidationArtifact {
        val result = validator.validate(sql)
        return SqlValidationArtifact(
            passed = result.passed,
            attempt = attempt,
            message = result.message,
            normalizedSql = result.normalizedSql,
        )
    }

    fun executeSql(
        executor: SqlExecutionService,
        statementId: String,
        sql: String,
    ): SqlResultReferenceArtifact {
        val result = executor.execute(statementId, sql)
        return SqlResultReferenceArtifact(
            statementId = result.statementId,
            resultId = result.resultId,
            rowCount = result.rowCount,
            columns = result.columns,
            truncated = result.truncated,
            notes = result.notes,
        )
    }
}
