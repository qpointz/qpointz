package io.qpointz.mill.ai.capabilities.sqlquery

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ValidationResult as HandlerValidationResult

/**
 * Application-level contract for validating a SQL string before it is treated as **generated SQL**
 * for downstream consumers (chat postprocessors, JDBC bridges, etc.).
 *
 * Implementations may delegate to Calcite, the Mill query engine, or a lightweight parser. They must
 * be **thread-safe** if the agent runtime invokes validation concurrently (default assumption:
 * single-threaded per conversation turn).
 *
 * The **`sql-query`** capability wires this contract into [SqlQueryToolHandlers.SqlValidationService]
 * via [asSqlValidationService] so tool handlers stay unchanged.
 *
 * **Execution** of validated SQL is **not** part of this interface; hosts run queries outside
 * `mill-ai-v3` after intercepting generated SQL artifacts.
 */
fun interface SqlValidator {

    /**
     * Validates the given SQL text.
     *
     * @param sql candidate SQL (may be empty; implementors typically fail blank input)
     * @return structural outcome; [SqlValidationOutcome.normalizedSql] may carry canonical SQL when
     * [SqlValidationOutcome.passed] is true
     */
    fun validate(sql: String): SqlValidationOutcome
}

/**
 * Result of [SqlValidator.validate], aligned with [HandlerValidationResult] for adapter mapping.
 *
 * @property passed whether validation succeeded
 * @property message optional human-readable failure or warning text
 * @property normalizedSql optional canonical SQL when validation passes
 */
data class SqlValidationOutcome(
    val passed: Boolean,
    val message: String? = null,
    val normalizedSql: String? = null,
)

/**
 * Maps this outcome to the handler [HandlerValidationResult] used by [SqlQueryToolHandlers.validateSql].
 */
fun SqlValidationOutcome.toValidationResult(): HandlerValidationResult =
    HandlerValidationResult(passed = passed, message = message, normalizedSql = normalizedSql)

/**
 * Maps a handler [HandlerValidationResult] to a [SqlValidationOutcome] (for tests and bridge code).
 */
fun HandlerValidationResult.toSqlValidationOutcome(): SqlValidationOutcome =
    SqlValidationOutcome(passed = passed, message = message, normalizedSql = normalizedSql)

/**
 * Adapts a [SqlValidator] to [SqlValidationService] for [SqlQueryCapability] wiring.
 */
fun SqlValidator.asSqlValidationService(): SqlValidationService =
    SqlValidationService { sql -> validate(sql).toValidationResult() }
