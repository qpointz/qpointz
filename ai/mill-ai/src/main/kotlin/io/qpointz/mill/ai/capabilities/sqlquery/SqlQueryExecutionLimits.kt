package io.qpointz.mill.ai.capabilities.sqlquery

/**
 * Default row limits for `sql-query` execution tools before host-specific configuration overrides.
 */
object SqlQueryExecutionLimits {
    const val DEFAULT_MAX_ROWS: Int = 1_000
    const val HARD_MAX_ROWS: Int = 5_000
}
