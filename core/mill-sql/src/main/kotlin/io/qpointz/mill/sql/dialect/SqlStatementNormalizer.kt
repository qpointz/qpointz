package io.qpointz.mill.sql.v2.dialect

/**
 * Normalizes SQL text according to dialect [StatementRules] before parse/validate/execute.
 */
object SqlStatementNormalizer {

    /** Calcite (Mill's parse engine) rejects trailing {@code ;} on single-statement submits. */
    private val DEFAULT_RULES = StatementRules(allowTrailingSemicolon = false)

    /**
     * Trims SQL and optionally removes trailing statement separators per dialect rules.
     *
     * @param sql raw SQL from the model or caller
     * @param spec active dialect spec; when null [DEFAULT_RULES] apply (strip trailing {@code ;})
     * @return normalized SQL suitable for single-statement parse
     */
    fun normalize(sql: String, spec: SqlDialectSpec? = null): String {
        var text = sql.trim()
        val rules = spec?.statements ?: DEFAULT_RULES
        if (!rules.allowTrailingSemicolon) {
            while (text.endsWith(";")) {
                text = text.removeSuffix(";").trimEnd()
            }
        }
        return text
    }
}
