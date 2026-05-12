package io.qpointz.mill.data.query.engine

import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.data.query.engine.marshal.ResultMarshallerRegistry
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.Vector
import io.qpointz.mill.proto.Vector.StringVector
import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.proto.VectorBlockSchema
import io.qpointz.mill.vectors.VectorBlockIterator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Unit tests for [DefaultQueryResultExecutionService] (page-window cache, tenant, epoch, paging, re-scan).
 */
class DefaultQueryResultExecutionServiceTest {

    private fun stringBlock(values: List<String>): VectorBlock {
        val stringVector = StringVector.newBuilder().addAllValues(values).build()
        val nulls = Vector.NullsVector.newBuilder().addAllNulls(values.map { false }).build()
        val vec = Vector.newBuilder()
            .setFieldIdx(0)
            .setNulls(nulls)
            .setStringVector(stringVector)
            .build()
        val field = Field.newBuilder()
            .setName("c")
            .setFieldIdx(0)
            .setType(
                DataType.newBuilder()
                    .setType(
                        LogicalDataType.newBuilder()
                            .setTypeId(LogicalDataType.LogicalDataTypeId.STRING)
                            .build(),
                    )
                    .setNullability(DataType.Nullability.NULL)
                    .build(),
            )
            .build()
        val schema = VectorBlockSchema.newBuilder().addFields(field).build()
        return VectorBlock.newBuilder()
            .setSchema(schema)
            .setVectorSize(values.size)
            .addVectors(vec)
            .build()
    }

    private fun iteratorOf(vararg blocks: VectorBlock): VectorBlockIterator {
        val list = blocks.toList()
        val it = list.iterator()
        return object : VectorBlockIterator {
            override fun schema(): VectorBlockSchema = list.first().schema

            override fun hasNext(): Boolean = it.hasNext()

            override fun next(): VectorBlock = it.next()

            override fun remove() {
                throw UnsupportedOperationException()
            }
        }
    }

    private fun defaultSettings(maxCachedPages: Int = 64) =
        QueryResultEngineSettings(
            maxMaterializedRows = 10_000,
            maxCachedPages = maxCachedPages,
            sessionExpireAfterAccess = java.time.Duration.ofMinutes(5),
            defaultFetchSize = 100,
            maxPageSize = 10,
        )

    @Test
    fun `should page backward on snapshot`() {
        val dispatcher = mock(DataOperationDispatcher::class.java)
        val block = stringBlock(listOf("r0", "r1", "r2", "r3"))
        `when`(dispatcher.execute(any())).thenReturn(iteratorOf(block))
        val registry = ResultMarshallerRegistry.load()
        val svc = DefaultQueryResultExecutionService(
            dispatcher,
            registry,
            defaultSettings(),
        )
        val alice = CallerContext("alice")
        val created = svc.create(alice, "select 1", QueryFormats.ROWS_OBJECTS, false, 10)
        val p1 = svc.getPage(alice, created.executionId, 1, 2, QueryFormats.ROWS_OBJECTS, null)
        assertThat(p1.pageIndex).isEqualTo(1)
        assertThat(p1.rowCount).isEqualTo(2)
        assertThat(p1.hasPrevious).isTrue()
        val p0 = svc.getPage(alice, created.executionId, 0, 2, QueryFormats.ROWS_OBJECTS, null)
        assertThat(p0.pageIndex).isZero()
        assertThat(p0.hasNext).isTrue()
        verify(dispatcher, times(1)).execute(any())
    }

    @Test
    fun `should forbid cross tenant read`() {
        val dispatcher = mock(DataOperationDispatcher::class.java)
        `when`(dispatcher.execute(any())).thenReturn(iteratorOf(stringBlock(listOf("x"))))
        val registry = ResultMarshallerRegistry.load()
        val svc = DefaultQueryResultExecutionService(
            dispatcher,
            registry,
            defaultSettings(),
        )
        val created = svc.create(CallerContext("alice"), "select 1", null, false, 10)
        assertThatThrownBy {
            svc.metadata(CallerContext("bob"), created.executionId)
        }.isInstanceOf(QuerySessionForbiddenException::class.java)
    }

    @Test
    fun `should increment epoch on replace and reject stale epoch`() {
        val dispatcher = mock(DataOperationDispatcher::class.java)
        `when`(dispatcher.execute(any()))
            .thenReturn(iteratorOf(stringBlock(listOf("a"))))
            .thenReturn(iteratorOf(stringBlock(listOf("b", "c"))))
        val registry = ResultMarshallerRegistry.load()
        val svc = DefaultQueryResultExecutionService(
            dispatcher,
            registry,
            defaultSettings(),
        )
        val ctx = CallerContext("alice")
        val created = svc.create(ctx, "select 1", null, false, 10)
        val rep = svc.replace(ctx, created.executionId, "select 2", null)
        assertThat(rep.epoch).isEqualTo(1)
        assertThatThrownBy {
            svc.getPage(ctx, created.executionId, 0, 10, null, 0)
        }.isInstanceOf(QueryEpochConflictException::class.java)
    }

    @Test
    fun `should not rescan when backward stays within M page window`() {
        val b1 = stringBlock(listOf("a", "b"))
        val b2 = stringBlock(listOf("c", "d"))
        val scan = iteratorOf(b1, b2)
        val dispatcher = mock(DataOperationDispatcher::class.java)
        `when`(dispatcher.execute(any())).thenReturn(scan)
        val registry = ResultMarshallerRegistry.load()
        val svc = DefaultQueryResultExecutionService(
            dispatcher,
            registry,
            defaultSettings(maxCachedPages = 2),
        )
        val ctx = CallerContext("alice")
        val created = svc.create(ctx, "select 1", QueryFormats.ROWS_OBJECTS, false, 10)
        assertThat(svc.metadata(ctx, created.executionId).totalResult).isNull()
        val p1 = svc.getPage(ctx, created.executionId, 1, 2, QueryFormats.ROWS_OBJECTS, null)
        assertThat(p1.rowCount).isEqualTo(2)
        val p0 = svc.getPage(ctx, created.executionId, 0, 2, QueryFormats.ROWS_OBJECTS, null)
        assertThat(p0.rowCount).isEqualTo(2)
        val p1b = svc.getPage(ctx, created.executionId, 1, 2, QueryFormats.ROWS_OBJECTS, null)
        assertThat(p1b.rowCount).isEqualTo(2)
        verify(dispatcher, times(1)).execute(any())
    }

    @Test
    fun `should rescan when backward target falls outside M page window`() {
        val b1 = stringBlock(listOf("a", "b"))
        val b2 = stringBlock(listOf("c", "d"))
        val scan1 = iteratorOf(b1, b2)
        val scan2 = iteratorOf(b1, b2)
        val dispatcher = mock(DataOperationDispatcher::class.java)
        `when`(dispatcher.execute(any())).thenReturn(scan1).thenReturn(scan2)
        val registry = ResultMarshallerRegistry.load()
        val svc = DefaultQueryResultExecutionService(
            dispatcher,
            registry,
            defaultSettings(maxCachedPages = 2),
        )
        val ctx = CallerContext("alice")
        val created = svc.create(ctx, "select 1", QueryFormats.ROWS_OBJECTS, false, 10)
        svc.getPage(ctx, created.executionId, 0, 2, QueryFormats.ROWS_OBJECTS, null)
        svc.getPage(ctx, created.executionId, 1, 2, QueryFormats.ROWS_OBJECTS, null)
        svc.getPage(ctx, created.executionId, 2, 2, QueryFormats.ROWS_OBJECTS, null)
        val p0 = svc.getPage(ctx, created.executionId, 0, 2, QueryFormats.ROWS_OBJECTS, null)
        assertThat(p0.rowCount).isEqualTo(2)
        verify(dispatcher, times(2)).execute(any())
    }

    @Test
    fun `should rescan when M is 1 and user pages backward`() {
        val block = stringBlock(listOf("a", "b", "c", "d"))
        val scan1 = iteratorOf(block)
        val scan2 = iteratorOf(block)
        val dispatcher = mock(DataOperationDispatcher::class.java)
        `when`(dispatcher.execute(any())).thenReturn(scan1).thenReturn(scan2)
        val registry = ResultMarshallerRegistry.load()
        val svc = DefaultQueryResultExecutionService(
            dispatcher,
            registry,
            defaultSettings(maxCachedPages = 1),
        )
        val ctx = CallerContext("alice")
        val created = svc.create(ctx, "select 1", QueryFormats.ROWS_OBJECTS, false, 10)
        svc.getPage(ctx, created.executionId, 1, 2, QueryFormats.ROWS_OBJECTS, null)
        svc.getPage(ctx, created.executionId, 0, 2, QueryFormats.ROWS_OBJECTS, null)
        verify(dispatcher, times(2)).execute(any())
    }
}
