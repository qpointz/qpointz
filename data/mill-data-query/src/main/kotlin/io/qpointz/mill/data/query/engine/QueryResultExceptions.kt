package io.qpointz.mill.data.query.engine

/**
 * Thrown when [io.qpointz.mill.data.query.engine.QueryResultExecutionService] cannot resolve a session id.
 *
 * @param executionId opaque id supplied by the client
 */
class QuerySessionNotFoundException(
    val executionId: String,
) : RuntimeException("Unknown execution: $executionId")

/**
 * Thrown when the caller tenant does not match the session tenant (v1 ownership model).
 *
 * @param executionId opaque id supplied by the client
 */
class QuerySessionForbiddenException(
    val executionId: String,
) : RuntimeException("Forbidden execution: $executionId")

/**
 * Thrown when an optional client [epoch] does not match the server session [epoch].
 *
 * @param executionId opaque id supplied by the client
 * @param clientEpoch epoch supplied by the client
 * @param serverEpoch current server epoch
 */
class QueryEpochConflictException(
    val executionId: String,
    val clientEpoch: Int,
    val serverEpoch: Int,
) : RuntimeException("Stale epoch for $executionId: client=$clientEpoch server=$serverEpoch")

/**
 * Thrown when SQL/plan execution fails after successful parse / dispatch setup.
 *
 * @param message human-readable error
 * @param cause underlying engine failure
 */
class QuerySqlExecutionException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
