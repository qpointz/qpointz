package io.qpointz.mill.source.statistics

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.RecordSchema

/**
 * Wires statistic providers for a resolved logical table without reading blob metadata.
 */
object SourceStatisticWiring {

    /**
     * Creates a blob-scoped record provider when [handler] implements [RecordStatisticReader].
     */
    fun recordStatisticProviderForBlob(
        handler: FormatHandler,
        blob: BlobPath,
        blobSource: BlobSource,
    ): RecordStatisticProvider? {
        val reader = handler as? RecordStatisticReader ?: return null
        return BlobBoundRecordStatisticProvider(reader, blob, blobSource)
    }

    /**
     * Builds the statistic provider holder for one logical table.
     *
     * Record statistics are wired only when **every** blob has a stats-capable handler.
     * Key statistics are wired when the schema contains an `id` column.
     */
    fun forTable(
        schema: RecordSchema,
        blobSource: BlobSource,
        readerBlobPairs: List<Pair<FormatHandler, List<BlobPath>>>,
    ): SourceTableStatisticProviders {
        val blobEntries = readerBlobPairs.flatMap { (handler, blobs) ->
            blobs.map { handler to it }
        }
        val record = wireRecordProvider(blobEntries, blobSource)
        val key = wireKeyProvider(schema)
        return SourceTableStatisticProviders.of(record = record, key = key)
    }

    private fun wireRecordProvider(
        blobEntries: List<Pair<FormatHandler, BlobPath>>,
        blobSource: BlobSource,
    ): RecordStatisticProvider? {
        if (blobEntries.isEmpty()) {
            return null
        }
        val leaves = blobEntries.map { (handler, blob) ->
            recordStatisticProviderForBlob(handler, blob, blobSource)
        }
        if (leaves.any { it == null }) {
            return null
        }
        return RecordStatisticAggregator(leaves.filterNotNull())
    }

    private fun wireKeyProvider(schema: RecordSchema): KeyStatisticProvider? {
        if (schema.field("id") == null) {
            return null
        }
        return SchemaKeyStatisticProvider(schema)
    }
}
