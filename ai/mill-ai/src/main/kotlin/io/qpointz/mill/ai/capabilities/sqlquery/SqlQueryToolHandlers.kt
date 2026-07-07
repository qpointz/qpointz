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
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import io.qpointz.mill.sql.v2.dialect.SqlStatementNormalizer

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
        val title: String? = null,
        val description: String? = null,
        val completionMode: String? = null,
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
     * Execution boundary for **host-side** query runners (outside the `sql-query` capability).
     *
     * The **`sql-query`** capability no longer invokes execution from agent tools; hosts intercept
     * generated SQL and call their own services. This interface remains for tests and adapters that
     * exercise [executeSql] or simulate result metadata without embedding execution in the LLM tool loop.
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
        title: String? = null,
        description: String? = null,
        completionMode: String? = null,
        dialectSpec: SqlDialectSpec? = null,
    ): SqlValidationArtifact {
        val contextError = validateSqlContext(title, description)
        if (contextError != null) {
            return SqlValidationArtifact(
                passed = false,
                attempt = attempt,
                message = contextError,
                normalizedSql = null,
                title = title,
                description = description,
                completionMode = completionMode ?: "sql-only",
            )
        }
        val normalizedInput = SqlStatementNormalizer.normalize(sql.trim(), dialectSpec)
        val result = validator.validate(normalizedInput)
        val normalizedSql = canonicalValidatedSql(
            passed = result.passed,
            normalizedFromValidator = result.normalizedSql ?: normalizedInput,
            dialectSpec = dialectSpec,
        )
        return SqlValidationArtifact(
            passed = result.passed,
            attempt = attempt,
            message = result.message,
            normalizedSql = normalizedSql,
            title = title?.trim(),
            description = description?.trim(),
            completionMode = completionMode?.trim()?.lowercase() ?: "sql-only",
        )
    }

    /**
     * Validates mandatory human context fields before SQL semantic validation.
     *
     * @param title short headline (3–120 chars after trim)
     * @param description plain-language summary (10–500 chars after trim)
     * @return error message when invalid, or null when valid
     */
    fun validateSqlContext(title: String?, description: String?): String? {
        val trimmedTitle = title?.trim().orEmpty()
        val trimmedDescription = description?.trim().orEmpty()
        if (trimmedTitle.isBlank()) return "title is required"
        if (trimmedDescription.isBlank()) return "description is required"
        if (trimmedTitle.length !in 3..120) return "title must be 3–120 characters"
        if (trimmedDescription.length !in 10..500) return "description must be 10–500 characters"
        return null
    }

    private fun canonicalValidatedSql(
        passed: Boolean,
        normalizedFromValidator: String?,
        dialectSpec: SqlDialectSpec?,
    ): String? {
        if (!passed) return null
        return normalizedFromValidator
            ?.takeIf { it.isNotBlank() }
            ?.let { SqlStatementNormalizer.normalize(it, dialectSpec).takeIf(String::isNotBlank) }
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

    /**
     * Structured SQL description artifact returned by `describe_sql`.
     */
    data class SqlDescriptionArtifact(
        val artifactType: String = "sql-description",
        val sql: String,
        val dialectId: String,
        val schema: List<Map<String, Any?>>,
        val warnings: List<String> = emptyList(),
        val source: Map<String, Any?>,
    )

    /**
     * Structured SQL execution artifact returned by `execute_sql`.
     */
    data class SqlExecutionArtifact(
        val artifactType: String = "sql-result",
        val sql: String,
        val dialectId: String,
        val schema: List<Map<String, Any?>>,
        val warnings: List<String> = emptyList(),
        val source: Map<String, Any?>,
        val rows: List<Map<String, Any?>>,
        val resultMode: String,
        val rowCount: Int,
        val truncated: Boolean,
        val hasMore: Boolean,
        val totalResult: Int?,
        val limit: Int,
    )

    /**
     * Describes validated SQL and returns schema metadata only.
     *
     * @param execution query-result execution port
     * @param sql validated SQL text
     * @param dialect optional dialect id
     * @param defaultMaxRows server default row cap for execute paths
     * @param hardMaxRows server hard row cap
     * @throws IllegalArgumentException when SQL is blank
     * @throws SqlQueryExecutionException when the execution plane fails
     */
    fun describeSql(
        execution: SqlQueryExecutionPort,
        sql: String,
        dialect: String?,
    ): SqlDescriptionArtifact {
        val submittedSql = sql.trim()
        require(submittedSql.isNotEmpty()) { "SQL statement is blank." }
        val request = SqlQueryExecutionRequest(
            sql = submittedSql,
            dialectId = dialect,
            maxRows = 1,
        )
        val probe = try {
            execution.describe(request)
        } catch (ex: SqlQueryExecutionException) {
            throw ex
        } catch (ex: Exception) {
            throw SqlQueryExecutionException("QUERY_FAILED", ex.message ?: "Query failed", ex)
        }
        return SqlDescriptionArtifact(
            sql = submittedSql,
            dialectId = probe.dialectId,
            schema = SqlQuerySchemaMapper.toOutputMaps(probe.schema),
            warnings = probe.warnings,
            source = probe.source,
        )
    }

    /**
     * Executes validated SQL with bounded paged or full semantics.
     *
     * @param execution query-result execution port
     * @param sql validated SQL text
     * @param resultMode paged or full (default paged)
     * @param maxRows optional caller row cap
     * @param dialect optional dialect id
     * @param pageIndex zero-based page index for paged mode
     * @param pageSize optional page size for paged mode
     * @param defaultMaxRows server default when [maxRows] is omitted
     * @param hardMaxRows server hard cap
     */
    fun executeSqlBounded(
        execution: SqlQueryExecutionPort,
        sql: String,
        resultMode: String?,
        maxRows: Int?,
        dialect: String?,
        pageIndex: Int = 0,
        pageSize: Int? = null,
        defaultMaxRows: Int = SqlQueryExecutionLimits.DEFAULT_MAX_ROWS,
        hardMaxRows: Int = SqlQueryExecutionLimits.HARD_MAX_ROWS,
    ): SqlExecutionArtifact {
        val submittedSql = sql.trim()
        require(submittedSql.isNotEmpty()) { "SQL statement is blank." }
        val mode = when (resultMode?.trim()?.lowercase()) {
            null, "", "paged" -> SqlQueryResultMode.PAGED
            "full" -> SqlQueryResultMode.FULL
            else -> throw IllegalArgumentException("Unsupported resultMode: $resultMode")
        }
        val effectiveMaxRows = (maxRows ?: defaultMaxRows).coerceIn(1, hardMaxRows)
        val request = SqlQueryExecutionRequest(
            sql = submittedSql,
            dialectId = dialect,
            resultMode = mode,
            maxRows = effectiveMaxRows,
            pageIndex = pageIndex,
            pageSize = pageSize,
        )
        val probe = try {
            execution.execute(request)
        } catch (ex: SqlQueryExecutionException) {
            throw ex
        } catch (ex: Exception) {
            throw SqlQueryExecutionException("QUERY_FAILED", ex.message ?: "Query failed", ex)
        }
        return SqlExecutionArtifact(
            sql = submittedSql,
            dialectId = probe.dialectId,
            schema = SqlQuerySchemaMapper.toOutputMaps(probe.schema),
            warnings = probe.warnings,
            source = probe.source,
            rows = probe.rows,
            resultMode = probe.resultMode.name.lowercase(),
            rowCount = probe.rowCount,
            truncated = probe.truncated,
            hasMore = probe.hasMore,
            totalResult = probe.totalResult,
            limit = probe.limit,
        )
    }
}




