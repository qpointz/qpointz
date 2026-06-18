package io.qpointz.mill.source.statistics

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

class RecordStatisticAggregatorTest {

    @Test
    fun shouldSumSingleProvider() {
        val aggregator = RecordStatisticAggregator(listOf(fixedProvider(7L)))
        assertEquals(7L, aggregator.recordStatistic()?.estimatedRowCount)
    }

    @Test
    fun shouldSumMultipleProviders() {
        val aggregator = RecordStatisticAggregator(listOf(fixedProvider(3L), fixedProvider(4L)))
        assertEquals(7L, aggregator.recordStatistic()?.estimatedRowCount)
    }

    @Test
    fun shouldReturnNull_whenAnyProviderUnknown() {
        val aggregator = RecordStatisticAggregator(
            listOf(fixedProvider(3L), fixedProvider(null)),
        )
        assertNull(aggregator.recordStatistic())
    }

    @Test
    fun shouldMemoizeAggregatedResult() {
        val calls = AtomicInteger(0)
        val counting = RecordStatisticProvider {
            calls.incrementAndGet()
            RecordStatistic(estimatedRowCount = 1L)
        }
        val aggregator = RecordStatisticAggregator(listOf(counting, fixedProvider(2L)))

        aggregator.recordStatistic()
        aggregator.recordStatistic()

        assertEquals(1, calls.get())
        assertEquals(3L, aggregator.recordStatistic()?.estimatedRowCount)
    }

    private fun fixedProvider(rowCount: Long?): RecordStatisticProvider =
        RecordStatisticProvider { RecordStatistic(estimatedRowCount = rowCount) }
}

class SchemaKeyStatisticProviderTest {

    @Test
    fun shouldExposeIdColumnAsUniqueKey() {
        val schema = RecordSchema.of(
            "id" to DatabaseType.i32(false),
            "name" to DatabaseType.string(true, 100),
        )
        val provider = SchemaKeyStatisticProvider(schema)

        val keys = provider.keyStatistic()?.uniqueKeys
        assertEquals(listOf(listOf(0)), keys)
    }

    @Test
    fun shouldReturnNull_whenNoIdColumn() {
        val schema = RecordSchema.of("name" to DatabaseType.string(true, 100))
        val provider = SchemaKeyStatisticProvider(schema)

        assertNull(provider.keyStatistic())
    }

    @Test
    fun shouldMemoizeKeyStatistic() {
        val schema = RecordSchema.of("id" to DatabaseType.i32(false))
        val provider = SchemaKeyStatisticProvider(schema)

        val first = provider.keyStatistic()
        val second = provider.keyStatistic()

        assertSame(first, second)
    }
}

class SourceStatisticWiringTest {

    @Test
    fun shouldWireRecordAndKeyProviders_forCapableHandler() {
        val schema = RecordSchema.of("id" to DatabaseType.i32(false))
        val blob = blobPath("/data/test.parquet")
        val providers = SourceStatisticWiring.forTable(
            schema = schema,
            blobSource = unusedBlobSource(),
            readerBlobPairs = listOf(CountingReader() to listOf(blob)),
        )

        assertEquals(false, providers.recordStatistic().isEmpty)
        assertEquals(false, providers.keyStatistic().isEmpty)
    }

    @Test
    fun shouldOmitRecordProvider_whenHandlerNotCapable() {
        val schema = RecordSchema.of("id" to DatabaseType.i32(false))
        val blob = blobPath("/data/test.csv")
        val providers = SourceStatisticWiring.forTable(
            schema = schema,
            blobSource = unusedBlobSource(),
            readerBlobPairs = listOf(NonCapableHandler() to listOf(blob)),
        )

        assertEquals(true, providers.recordStatistic().isEmpty)
        assertEquals(false, providers.keyStatistic().isEmpty)
    }

    @Test
    fun shouldOmitKeyProvider_whenSchemaHasNoIdColumn() {
        val schema = RecordSchema.of("name" to DatabaseType.string(true, 100))
        val blob = blobPath("/data/test.parquet")
        val providers = SourceStatisticWiring.forTable(
            schema = schema,
            blobSource = unusedBlobSource(),
            readerBlobPairs = listOf(CountingReader() to listOf(blob)),
        )

        assertEquals(false, providers.recordStatistic().isEmpty)
        assertEquals(true, providers.keyStatistic().isEmpty)
    }

    @Test
    fun shouldMemoizeBlobStatisticRead() {
        val schema = RecordSchema.of("id" to DatabaseType.i32(false))
        val blob = blobPath("/data/test.parquet")
        val reader = CountingReader()
        val provider = SourceStatisticWiring.recordStatisticProviderForBlob(
            reader,
            blob,
            unusedBlobSource(),
        )!!

        provider.recordStatistic()
        provider.recordStatistic()

        assertEquals(1, reader.readCount)
    }

    private fun blobPath(uriPath: String): BlobPath = object : BlobPath {
        override val uri: URI = URI.create("file://$uriPath")
    }

    private fun unusedBlobSource(): BlobSource = object : BlobSource {
        override fun listBlobs(): Sequence<BlobPath> = emptySequence()
        override fun openInputStream(path: BlobPath): java.io.InputStream {
            throw UnsupportedOperationException("not used in wiring tests")
        }
        override fun openSeekableChannel(path: BlobPath): java.nio.channels.SeekableByteChannel {
            throw UnsupportedOperationException("not used in wiring tests")
        }
        override fun close() {}
    }

    private class CountingReader : io.qpointz.mill.source.FormatHandler, RecordStatisticReader {
        var readCount: Int = 0

        override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema =
            RecordSchema.of("id" to DatabaseType.i32(false))

        override fun createRecordSource(
            blob: BlobPath,
            blobSource: BlobSource,
            schema: RecordSchema,
        ): io.qpointz.mill.source.RecordSource {
            throw UnsupportedOperationException("not used in wiring tests")
        }

        override fun readRecordStatistic(blob: BlobPath, blobSource: BlobSource): RecordStatistic {
            readCount++
            return RecordStatistic(estimatedRowCount = 5L)
        }
    }

    private class NonCapableHandler : io.qpointz.mill.source.FormatHandler {
        override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema =
            RecordSchema.of("id" to DatabaseType.i32(false))

        override fun createRecordSource(
            blob: BlobPath,
            blobSource: BlobSource,
            schema: RecordSchema,
        ): io.qpointz.mill.source.RecordSource {
            throw UnsupportedOperationException("not used in wiring tests")
        }
    }
}
