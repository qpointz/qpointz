package io.qpointz.mill.data.query.engine

import com.github.benmanes.caffeine.cache.Caffeine
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.data.query.engine.marshal.ResultMarshallerRegistry
import io.qpointz.mill.proto.QueryExecutionConfig
import io.qpointz.mill.proto.QueryRequest
import io.qpointz.mill.proto.SQLStatement
import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.proto.VectorBlockSchema
import io.qpointz.mill.vectors.VectorBlockIterator
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.max
import kotlin.math.min

/**
 * [QueryResultExecutionService] that pulls [VectorBlock]s lazily from [DataOperationDispatcher.execute]
 * and retains at most [QueryResultEngineSettings.maxCachedPages] **presentation pages** per session
 * (page size is fixed from the first materializing [getPage] / create first page).
 *
 * Forward [getPage] scrolls the iterator until the requested page is available, then trims leading rows
 * so the lowest retained page index is at least `max(0, pageIndex - (M - 1))` for `M = maxCachedPages`.
 * Backward requests within that window are served without re-query. If the page is no longer in memory,
 * the same SQL is executed again and rows are read until the requested page plus up to `M - 1` following
 * pages are cached (for forward continuation), then the same low-edge trim is applied.
 *
 * [SessionMetadata.totalResult] stays `null` until the iterator for the current scan is exhausted.
 *
 * @param dispatcher data-plane dispatcher used for `execute`.
 * @param marshallerRegistry marshaller lookup for `getPage`.
 * @param settings engine tuning (typically bound from `mill.data.query.*`).
 */
class DefaultQueryResultExecutionService(
    private val dispatcher: DataOperationDispatcher,
    private val marshallerRegistry: ResultMarshallerRegistry,
    private val settings: QueryResultEngineSettings,
) : QueryResultExecutionService {

    private class Session(
        val tenant: String,
        val lock: ReentrantReadWriteLock = ReentrantReadWriteLock(),
        @Volatile var epoch: Int = 0,
        @Volatile var sql: String,
        @Volatile var defaultFormat: String?,
        @Volatile var iterator: VectorBlockIterator?,
        val blocks: MutableList<VectorBlock> = mutableListOf(),
        /** Global index of the first visible row in [blocks] (after [physicalLeadSkipRows]). */
        @Volatile var bufferStartRow: Int = 0,
        /** Rows to skip at the physical start of [blocks].first() before the visible stream begins. */
        @Volatile var physicalLeadSkipRows: Int = 0,
        /** One past the last row index read from the current iterator (cumulative for this scan). */
        @Volatile var rowsFetchedExclusive: Int = 0,
        @Volatile var exhausted: Boolean = false,
        /** Normalized page size for cache windowing; fixed after first materializing page request. */
        @Volatile var cachePageSize: Int? = null,
        /** Schema for the current scan (iterator or first block); kept for empty pages. */
        @Volatile var scanSchema: VectorBlockSchema? = null,
    )

    private val cache = Caffeine.newBuilder()
        .expireAfterAccess(settings.sessionExpireAfterAccess)
        .build<String, Session>()

    override fun create(
        context: CallerContext,
        sql: String,
        defaultFormat: String?,
        includeFirstPage: Boolean,
        firstPageSize: Int,
    ): CreateSessionResult {
        val session = Session(
            tenant = context.tenant,
            sql = sql,
            defaultFormat = defaultFormat ?: QueryFormats.ROWS_OBJECTS,
            iterator = null,
        )
        openIterator(session, sql)
        val id = UUID.randomUUID().toString()
        cache.put(id, session)
        val meta = toMetadata(id, session)
        val first = if (includeFirstPage) {
            val ps = normalizePageSize(firstPageSize)
            session.cachePageSize = ps
            getPage(context, id, 0, ps, null, null)
        } else {
            null
        }
        return CreateSessionResult(executionId = id, epoch = session.epoch, metadata = meta, firstPage = first)
    }

    override fun replace(
        context: CallerContext,
        executionId: String,
        sql: String,
        defaultFormat: String?,
    ): ReplaceSessionResult {
        val session = load(executionId)
        session.lock.writeLock().lock()
        try {
            assertTenant(session, context, executionId)
            session.sql = sql
            if (defaultFormat != null) {
                session.defaultFormat = defaultFormat
            }
            session.cachePageSize = null
            openIterator(session, sql)
            session.epoch += 1
            return ReplaceSessionResult(epoch = session.epoch)
        } finally {
            session.lock.writeLock().unlock()
        }
    }

    override fun metadata(context: CallerContext, executionId: String): SessionMetadata {
        val session = load(executionId)
        session.lock.readLock().lock()
        try {
            assertTenant(session, context, executionId)
            return toMetadata(executionId, session)
        } finally {
            session.lock.readLock().unlock()
        }
    }

    override fun getPage(
        context: CallerContext,
        executionId: String,
        pageIndex: Int,
        pageSize: Int,
        formatId: String?,
        clientEpoch: Int?,
    ): PagedQueryPayload {
        if (pageIndex < 0) {
            throw IllegalArgumentException("pageIndex must be >= 0")
        }
        val session = load(executionId)
        session.lock.writeLock().lock()
        try {
            assertTenant(session, context, executionId)
            if (clientEpoch != null && clientEpoch != session.epoch) {
                throw QueryEpochConflictException(executionId, clientEpoch, session.epoch)
            }
            val fmt = (formatId ?: session.defaultFormat ?: QueryFormats.ROWS_OBJECTS).lowercase()
            val marshaller = marshallerRegistry.byFormatId(fmt)
                ?: throw IllegalArgumentException("Unknown format: $fmt")
            val ps = normalizePageSize(pageSize)
            bindOrResetCachePageSize(session, ps)
            val m = settings.maxCachedPages.coerceAtLeast(1)
            val globalStart = pageIndex * ps
            if (globalStart < session.bufferStartRow) {
                reopenForBackwardMiss(session, pageIndex, ps, m)
            } else {
                materializeForwardWindow(session, pageIndex, ps, m)
            }
            trimWindowLowEdge(session, ps, pageIndex, m)
            val bufRows = rowsInBuffer(session)
            val bufEnd = session.bufferStartRow + bufRows
            if (session.exhausted && globalStart >= session.rowsFetchedExclusive) {
                return emptyPage(
                    session.epoch,
                    pageIndex,
                    ps,
                    session.rowsFetchedExclusive,
                    columnSchemaForSession(session),
                )
            }
            if (globalStart >= bufEnd) {
                return emptyPage(
                    session.epoch,
                    pageIndex,
                    ps,
                    if (session.exhausted) session.rowsFetchedExclusive else null,
                    columnSchemaForSession(session),
                )
            }
            val remainingInBuffer = bufEnd - globalStart
            val rowCount = min(ps, remainingInBuffer)
            val localStart = globalStart - session.bufferStartRow
            val baos = ByteArrayOutputStream()
            marshaller.writePage(session.blocks, session.physicalLeadSkipRows + localStart, rowCount, baos)
            val total = if (session.exhausted) session.rowsFetchedExclusive else null
            val hasPrevious = pageIndex > 0
            val globalEnd = globalStart + rowCount
            val iterHasNext = session.iterator?.hasNext() == true
            val hasNext = when {
                rowCount == 0 -> false
                session.exhausted -> globalEnd < session.rowsFetchedExclusive
                else ->
                    rowCount < ps ||
                        globalEnd < bufEnd ||
                        (rowCount == ps && iterHasNext)
            }
            return PagedQueryPayload(
                epoch = session.epoch,
                pageIndex = pageIndex,
                pageSize = ps,
                rowCount = rowCount,
                totalResult = total,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                contentType = marshaller.contentType,
                columnSchema = columnSchemaForSession(session),
                body = baos.toByteArray(),
            )
        } finally {
            session.lock.writeLock().unlock()
        }
    }

    override fun delete(context: CallerContext, executionId: String) {
        val session = cache.getIfPresent(executionId) ?: throw QuerySessionNotFoundException(executionId)
        session.lock.writeLock().lock()
        try {
            assertTenant(session, context, executionId)
        } finally {
            session.lock.writeLock().unlock()
        }
        cache.invalidate(executionId)
    }

    private fun emptyPage(
        epoch: Int,
        pageIndex: Int,
        pageSize: Int,
        total: Int?,
        columnSchema: List<Map<String, Any?>>,
    ): PagedQueryPayload {
        val marshaller = marshallerRegistry.byFormatId(QueryFormats.ROWS_OBJECTS)!!
        val baos = ByteArrayOutputStream()
        marshaller.writePage(emptyList(), 0, 0, baos)
        return PagedQueryPayload(
            epoch = epoch,
            pageIndex = pageIndex,
            pageSize = pageSize,
            rowCount = 0,
            totalResult = total,
            hasPrevious = pageIndex > 0,
            hasNext = false,
            contentType = marshaller.contentType,
            columnSchema = columnSchema,
            body = baos.toByteArray(),
        )
    }

    /**
     * Column metadata for the HTTP `schema` envelope: prefer a materialized block, else iterator schema.
     */
    private fun columnSchemaForSession(session: Session): List<Map<String, Any?>> {
        val proto = session.blocks.firstOrNull()?.schema ?: session.scanSchema
        return proto?.toQueryResultSchemaColumns() ?: emptyList()
    }

    private fun toMetadata(id: String, session: Session): SessionMetadata =
        SessionMetadata(
            executionId = id,
            epoch = session.epoch,
            totalResult = if (session.exhausted) session.rowsFetchedExclusive else null,
            defaultFormat = session.defaultFormat,
        )

    private fun normalizePageSize(pageSize: Int): Int {
        if (pageSize <= 0) {
            throw IllegalArgumentException("pageSize must be > 0")
        }
        return min(pageSize, settings.maxPageSize)
    }

    private fun load(executionId: String): Session =
        cache.getIfPresent(executionId) ?: throw QuerySessionNotFoundException(executionId)

    private fun assertTenant(session: Session, context: CallerContext, executionId: String) {
        if (session.tenant != context.tenant) {
            throw QuerySessionForbiddenException(executionId)
        }
    }

    private fun buildRequest(sql: String): QueryRequest =
        QueryRequest.newBuilder()
            .setStatement(SQLStatement.newBuilder().setSql(sql).build())
            .setConfig(
                QueryExecutionConfig.newBuilder()
                    .setFetchSize(settings.defaultFetchSize)
                    .build(),
            )
            .build()

    private fun openIterator(session: Session, sql: String) {
        session.blocks.clear()
        session.bufferStartRow = 0
        session.physicalLeadSkipRows = 0
        session.rowsFetchedExclusive = 0
        session.exhausted = false
        session.iterator = null
        val it = try {
            dispatcher.execute(buildRequest(sql))
        } catch (ex: Exception) {
            throw QuerySqlExecutionException(ex.message ?: "execute failed", ex)
        }
        session.iterator = it
        session.scanSchema = it.schema()
        if (!it.hasNext()) {
            session.iterator = null
            session.exhausted = true
        }
    }

    private fun rowsInBuffer(session: Session): Int =
        session.rowsFetchedExclusive - session.bufferStartRow

    /**
     * Drops leading visible rows until [targetGlobalStart] is the first visible row (used for page-window eviction).
     */
    private fun trimToGlobalStart(session: Session, targetGlobalStart: Int) {
        var target = targetGlobalStart
        if (target <= session.bufferStartRow) {
            return
        }
        while (session.bufferStartRow < target && session.blocks.isNotEmpty()) {
            val first = session.blocks.first()
            val avail = first.vectorSize - session.physicalLeadSkipRows
            val need = target - session.bufferStartRow
            val step = min(need, avail)
            session.bufferStartRow += step
            session.physicalLeadSkipRows += step
            if (session.physicalLeadSkipRows >= first.vectorSize) {
                session.blocks.removeAt(0)
                session.physicalLeadSkipRows = 0
            }
        }
    }

    private fun bindOrResetCachePageSize(session: Session, ps: Int) {
        val existing = session.cachePageSize
        if (existing == null) {
            session.cachePageSize = ps
        } else if (existing != ps) {
            session.cachePageSize = ps
            openIterator(session, session.sql)
        }
    }

    private fun materializeForwardWindow(session: Session, pageIndex: Int, ps: Int, m: Int) {
        dropLeadingBlocksBeforePageStart(session, pageIndex, ps)
        val trimStartPage = max(0, pageIndex - (m - 1))
        val windowEndExclusive = (trimStartPage + m) * ps
        val needExclusive = max((pageIndex + 1) * ps, windowEndExclusive)
        while (session.rowsFetchedExclusive < needExclusive && !session.exhausted) {
            if (!pullNextBlock(session)) {
                break
            }
        }
    }

    /**
     * Removes whole leading blocks whose **visible** span ends at or before [pageIndex] * [ps]
     * so the first retained row is not past the requested page.
     */
    private fun dropLeadingBlocksBeforePageStart(session: Session, pageIndex: Int, ps: Int) {
        val pageStart = pageIndex * ps
        while (session.blocks.isNotEmpty()) {
            val visibleInFirst = session.blocks.first().vectorSize - session.physicalLeadSkipRows
            val firstVisibleEndExclusive = session.bufferStartRow + visibleInFirst
            if (firstVisibleEndExclusive <= pageStart) {
                session.bufferStartRow += visibleInFirst
                session.blocks.removeAt(0)
                session.physicalLeadSkipRows = 0
            } else {
                break
            }
        }
    }

    /**
     * Re-executes SQL and reads ahead until page [pageIndex] is available and up to `M - 1` following pages
     * are materialized when the result is long enough (bounded by [maxMaterializedRows]).
     */
    private fun reopenForBackwardMiss(session: Session, pageIndex: Int, ps: Int, m: Int) {
        openIterator(session, session.sql)
        val prefetchEndExclusive = (pageIndex + m) * ps
        while (session.rowsFetchedExclusive < prefetchEndExclusive && !session.exhausted) {
            if (!pullNextBlock(session)) {
                break
            }
        }
        while (session.rowsFetchedExclusive < (pageIndex + 1) * ps && !session.exhausted) {
            if (!pullNextBlock(session)) {
                break
            }
        }
    }

    private fun trimWindowLowEdge(session: Session, ps: Int, pageIndex: Int, m: Int) {
        val trimStartPage = max(0, pageIndex - (m - 1))
        trimToGlobalStart(session, trimStartPage * ps)
    }

    private fun pullNextBlock(session: Session): Boolean {
        val it = session.iterator ?: return false
        if (!it.hasNext()) {
            session.iterator = null
            session.exhausted = true
            return false
        }
        val block = it.next()
        val add = block.vectorSize
        if (session.rowsFetchedExclusive + add > settings.maxMaterializedRows) {
            throw QuerySqlExecutionException(
                "Result exceeds maxMaterializedRows=${settings.maxMaterializedRows}",
            )
        }
        session.blocks.add(block)
        session.rowsFetchedExclusive += add
        return true
    }
}
