package io.qpointz.mill.data.query.engine

/**
 * Programmatic query-result sessions over a [DataOperationDispatcher].
 */
interface QueryResultExecutionService {

    /**
     * Creates a new session and optionally returns the first page.
     *
     * @param context caller identity (tenant required).
     * @param sql SQL text executed against the active backend.
     * @param defaultFormat optional default marshaller id for later `getPage` calls.
     * @param includeFirstPage when true, the first page (`pageIndex` 0) is included in the result.
     * @param firstPageSize presentation `pageSize` for the optional first page.
     */
    fun create(
        context: CallerContext,
        sql: String,
        defaultFormat: String?,
        includeFirstPage: Boolean,
        firstPageSize: Int,
    ): CreateSessionResult

    /**
     * Replaces SQL for an existing session id, increments [SessionMetadata.epoch], and rebuilds buffers.
     */
    fun replace(
        context: CallerContext,
        executionId: String,
        sql: String,
        defaultFormat: String?,
    ): ReplaceSessionResult

    /**
     * Returns stable metadata for a session (epoch, totals, hints).
     */
    fun metadata(context: CallerContext, executionId: String): SessionMetadata

    /**
     * Returns one presentation page as marshaller bytes plus paging envelope fields.
     *
     * @param formatId explicit marshaller id override or null to use session default / negotiation done in HTTP.
     * @param clientEpoch optional optimistic concurrency token; mismatch throws [QueryEpochConflictException].
     */
    fun getPage(
        context: CallerContext,
        executionId: String,
        pageIndex: Int,
        pageSize: Int,
        formatId: String?,
        clientEpoch: Int?,
    ): PagedQueryPayload

    /**
     * Deletes a session and releases buffers.
     */
    fun delete(context: CallerContext, executionId: String)
}

/**
 * Result of [QueryResultExecutionService.create].
 *
 * @property executionId opaque session id.
 * @property epoch initial epoch (always 0 on create).
 * @property metadata session metadata snapshot after materialization.
 * @property firstPage populated when [QueryResultExecutionService.create] was called with `includeFirstPage = true`.
 */
data class CreateSessionResult(
    val executionId: String,
    val epoch: Int,
    val metadata: SessionMetadata,
    val firstPage: PagedQueryPayload?,
)

/**
 * Result of [QueryResultExecutionService.replace].
 *
 * @property epoch new epoch after successful replace.
 */
data class ReplaceSessionResult(
    val epoch: Int,
)

/**
 * Session metadata returned by [QueryResultExecutionService.metadata].
 *
 * @property epoch monotonic generation counter (starts at 0).
 * @property totalResult total rows when known; `null` when unknown (baseline full materialization sets a number).
 * @property defaultFormat marshaller id default for this session.
 */
data class SessionMetadata(
    val executionId: String,
    val epoch: Int,
    val totalResult: Int?,
    val defaultFormat: String?,
)

/**
 * One marshaller-encoded page plus paging envelope fields for HTTP mapping.
 */
data class PagedQueryPayload(
    val epoch: Int,
    val pageIndex: Int,
    val pageSize: Int,
    val rowCount: Int,
    val totalResult: Int?,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val contentType: String,
    val body: ByteArray,
)
