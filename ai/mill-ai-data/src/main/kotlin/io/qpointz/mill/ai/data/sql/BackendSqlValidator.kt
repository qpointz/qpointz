package io.qpointz.mill.ai.data.sql

import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidationOutcome
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import io.qpointz.mill.sql.v2.dialect.SqlStatementNormalizer

/**
 * [SqlValidator] that delegates to the Mill data backend’s SQL parser ([SqlProvider.parseSql]).
 *
 * Validation follows whatever parse/plan rules the active backend applies (including catalog and
 * dialect resolution owned by that backend). This is the **default** implementation wired by
 * [io.qpointz.mill.ai.autoconfigure.AiV3DataAutoConfiguration] when a [SqlProvider] bean exists.
 *
 * @param sqlProvider active Mill data backend used for `parseSql` / plan building
 * @param dialectSpec optional active dialect spec for statement normalization (e.g. trailing `;`)
 */
class BackendSqlValidator(
    private val sqlProvider: SqlProvider,
    private val dialectSpec: SqlDialectSpec? = null,
) : SqlValidator {

    override fun validate(sql: String): SqlValidationOutcome {
        val normalized = SqlStatementNormalizer.normalize(sql, dialectSpec)
        // Backend-owned parse: success means a plan was built; failure carries the parser/planner message.
        val parseResult = sqlProvider.parseSql(normalized)
        return SqlValidationOutcome(
            passed = parseResult.isSuccess,
            message = if (parseResult.isSuccess) null else parseResult.message,
            normalizedSql = if (parseResult.isSuccess) normalized.takeIf { it.isNotEmpty() } else null,
        )
    }
}
