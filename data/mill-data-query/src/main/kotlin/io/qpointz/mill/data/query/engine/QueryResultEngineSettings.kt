package io.qpointz.mill.data.query.engine

import java.time.Duration

/**
 * Runtime tuning for [DefaultQueryResultExecutionService] (mapped from `mill.data.query.*` in Spring).
 *
 * @property maxMaterializedRows Hard cap on rows materialized per session (full snapshot path).
 * @property sessionExpireAfterAccess Idle eviction for the session cache.
 * @property defaultFetchSize Initial `QueryExecutionConfig.fetchSize` for dispatcher `execute`.
 * @property maxPageSize Upper bound for presentation `pageSize` accepted by the service.
 */
data class QueryResultEngineSettings(
    val maxMaterializedRows: Int = 100_000,
    val sessionExpireAfterAccess: Duration = Duration.ofMinutes(30),
    val defaultFetchSize: Int = 1024,
    val maxPageSize: Int = 10_000,
)
