package io.qpointz.mill.data.query.engine

import com.github.benmanes.caffeine.cache.Caffeine
import io.qpointz.mill.data.query.engine.marshal.ResultMarshallerRegistry
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.proto.QueryExecutionConfig
import io.qpointz.mill.proto.QueryRequest
import io.qpointz.mill.proto.SQLStatement
import io.qpointz.mill.proto.VectorBlock
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.min

/**
 * Default [QueryResultExecutionService] that materializes full results into memory (bounded by [QueryResultEngineSettings.maxMaterializedRows]).
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

    private data class Session(
        val tenant: String,
        val lock: ReentrantReadWriteLock = ReentrantReadWriteLock(),
        @Volatile var epoch: Int = 0,
        @Volatile var blocks: List<VectorBlock> = emptyList(),
        @Volatile var totalRows: Int = 0,
        @Volatile var sql: String,
        @Volatile var defaultFormat: String?,
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
        )
        materializeInto(session, sql)
        val id = UUID.randomUUID().toString()
        cache.put(id, session)
        val meta = toMetadata(id, session)
        val first = if (includeFirstPage) {
            getPage(context, id, 0, normalizePageSize(firstPageSize), null, null)
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
            materializeInto(session, sql)
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
        session.lock.readLock().lock()
        try {
            assertTenant(session, context, executionId)
            if (clientEpoch != null && clientEpoch != session.epoch) {
                throw QueryEpochConflictException(executionId, clientEpoch, session.epoch)
            }
            val fmt = (formatId ?: session.defaultFormat ?: QueryFormats.ROWS_OBJECTS).lowercase()
            val marshaller = marshallerRegistry.byFormatId(fmt)
                ?: throw IllegalArgumentException("Unknown format: $fmt")
            val ps = normalizePageSize(pageSize)
            val globalStart = pageIndex * ps
            if (globalStart > session.totalRows) {
                return emptyPage(session.epoch, pageIndex, ps, session.totalRows)
            }
            val remaining = session.totalRows - globalStart
            val rowCount = min(ps, remaining)
            val baos = ByteArrayOutputStream()
            marshaller.writePage(session.blocks, globalStart, rowCount, baos)
            val total = session.totalRows
            val hasPrevious = pageIndex > 0
            val hasNext = globalStart + rowCount < total
            return PagedQueryPayload(
                epoch = session.epoch,
                pageIndex = pageIndex,
                pageSize = ps,
                rowCount = rowCount,
                totalResult = total,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                contentType = marshaller.contentType,
                body = baos.toByteArray(),
            )
        } finally {
            session.lock.readLock().unlock()
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

    private fun emptyPage(epoch: Int, pageIndex: Int, pageSize: Int, total: Int?): PagedQueryPayload {
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
            body = baos.toByteArray(),
        )
    }

    private fun toMetadata(id: String, session: Session): SessionMetadata =
        SessionMetadata(
            executionId = id,
            epoch = session.epoch,
            totalResult = session.totalRows,
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

    private fun materializeInto(session: Session, sql: String) {
        val (blocks, rows) = materialize(sql)
        session.blocks = blocks
        session.totalRows = rows
    }

    private fun materialize(sql: String): Pair<List<VectorBlock>, Int> {
        val request = QueryRequest.newBuilder()
            .setStatement(SQLStatement.newBuilder().setSql(sql).build())
            .setConfig(
                QueryExecutionConfig.newBuilder()
                    .setFetchSize(settings.defaultFetchSize)
                    .build(),
            )
            .build()
        val iterator = try {
            dispatcher.execute(request)
        } catch (ex: Exception) {
            throw QuerySqlExecutionException(ex.message ?: "execute failed", ex)
        }
        val blocks = ArrayList<VectorBlock>()
        var rows = 0
        while (iterator.hasNext()) {
            val block = iterator.next()
            val add = block.vectorSize
            if (rows + add > settings.maxMaterializedRows) {
                throw QuerySqlExecutionException(
                    "Result exceeds maxMaterializedRows=${settings.maxMaterializedRows}",
                )
            }
            blocks.add(block)
            rows += add
        }
        return blocks to rows
    }
}
