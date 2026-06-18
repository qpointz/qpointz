package io.qpointz.mill.source.statistics

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource

/**
 * Format opt-in for reading record statistics from a single blob without a full scan.
 *
 * Implemented by format handlers that can read row counts from file metadata (for example Parquet).
 */
fun interface RecordStatisticReader {

    /**
     * Reads record statistics for [blob].
     *
     * @param blob blob to inspect
     * @param blobSource storage access for the blob
     */
    fun readRecordStatistic(blob: BlobPath, blobSource: BlobSource): RecordStatistic?
}
