package io.qpointz.mill.source.export

import io.qpointz.mill.vectors.VectorBlockIterator
import java.io.OutputStream

/**
 * Encodes a columnar [VectorBlockIterator] into bytes on [OutputStream] without executing queries.
 */
fun interface StreamingExportEncoder {

    /**
     * Drains [iterator] and writes encoded bytes to [out]. Implementations must not buffer the full
     * result set in memory.
     *
     * @param iterator query result blocks
     * @param out response or file stream
     */
    fun encode(iterator: VectorBlockIterator, out: OutputStream)
}
