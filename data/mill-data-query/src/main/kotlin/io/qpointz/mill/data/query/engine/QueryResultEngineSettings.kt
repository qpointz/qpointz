package io.qpointz.mill.data.query.engine

import java.time.Duration

/**
 * Runtime tuning for [DefaultQueryResultExecutionService] (mapped from `mill.data.query.*` in Spring).
 *
 * @property maxMaterializedRows Hard cap on rows read from the dispatcher iterator **per scan**
 * (one `execute` / backward re-scan); prevents runaway reads.
 * @property maxCachedPages Maximum number of **presentation pages** (fixed session page size from the first
 * materializing `getPage` or create-with-first-page) kept for backward paging without re-query; forward
 * scans prefetch within the same **M**-page row window when possible.
 * @property sessionExpireAfterAccess Idle eviction for the session cache.
 * @property defaultFetchSize Initial `QueryExecutionConfig.fetchSize` for dispatcher `execute`.
 * @property maxPageSize Upper bound for presentation `pageSize` accepted by the service.
 */
data class QueryResultEngineSettings(
    val maxMaterializedRows: Int = 100_000,
    val maxCachedPages: Int = 16,
    val sessionExpireAfterAccess: Duration = Duration.ofMinutes(30),
    val defaultFetchSize: Int = 1024,
    val maxPageSize: Int = 10_000,
)
