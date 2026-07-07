package io.qpointz.mill.ai.autoconfigure.sqlquery

import io.qpointz.mill.data.query.engine.CallerContext

/**
 * Resolves [CallerContext] for in-process `sql-query` execution tool invocations.
 */
fun interface SqlQueryExecutionCallerContextResolver {

    /**
     * @return tenant-scoped caller identity for query-result session ownership
     */
    fun resolve(): CallerContext
}
