package io.qpointz.mill.source.statistics

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource

/**
 * Blob-scoped [RecordStatisticProvider] that memoizes the first read from [reader].
 */
internal class BlobBoundRecordStatisticProvider(
    private val reader: RecordStatisticReader,
    private val blob: BlobPath,
    private val blobSource: BlobSource,
) : RecordStatisticProvider {

    private val cached: RecordStatistic? by lazy {
        reader.readRecordStatistic(blob, blobSource)
    }

    override fun recordStatistic(): RecordStatistic? = cached
}
