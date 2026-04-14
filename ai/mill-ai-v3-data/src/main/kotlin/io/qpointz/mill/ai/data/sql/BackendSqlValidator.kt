package io.qpointz.mill.ai.data.sql

import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidationOutcome
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.data.backend.SqlProvider

/**
 * [SqlValidator] that delegates to the Mill data backend’s SQL parser ([SqlProvider.parseSql]).
 *
 * Validation follows whatever parse/plan rules the active backend applies (including catalog and
 * dialect resolution owned by that backend). This is the **default** implementation wired by
 * [io.qpointz.mill.ai.autoconfigure.MillAiV3DataAutoConfiguration] when a [SqlProvider] bean exists.
 *
 * @param sqlProvider active Mill data backend used for `parseSql` / plan building
 */
class BackendSqlValidator(
    private val sqlProvider: SqlProvider,
) : SqlValidator {

    override fun validate(sql: String): SqlValidationOutcome {
        // Backend-owned parse: success means a plan was built; failure carries the parser/planner message.
        val parseResult = sqlProvider.parseSql(sql)
        return SqlValidationOutcome(
            parseResult.isSuccess,
            message = if (parseResult.isSuccess) null else parseResult.message,
        )
    }
}
