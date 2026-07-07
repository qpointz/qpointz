package io.qpointz.mill.ai.capabilities.sqlquery

/**
 * Placeholder execution port used when no query-result adapter is registered.
 */
object UnsupportedSqlQueryExecutionPort : SqlQueryExecutionPort {
    override fun describe(request: SqlQueryExecutionRequest): SqlDescriptionProbeResult =
        notConfigured()

    override fun execute(request: SqlQueryExecutionRequest): SqlExecutionProbeResult =
        notConfigured()

    private fun notConfigured(): Nothing =
        throw SqlQueryExecutionException(
            "QUERY_FAILED",
            "SqlQueryExecutionPort is not configured on the application classpath",
        )
}
