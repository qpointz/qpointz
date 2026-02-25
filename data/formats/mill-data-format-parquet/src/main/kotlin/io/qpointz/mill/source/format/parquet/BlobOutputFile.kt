package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSink
import org.apache.parquet.io.OutputFile
import org.apache.parquet.io.PositionOutputStream
import java.io.OutputStream

/**
 * Storage-agnostic [OutputFile] that writes via [BlobSink].
 *
 * Bridges the Mill storage abstraction to Parquet's I/O interface,
 * enabling Parquet writes to any backend (local, ADLS, S3, etc.).
 *
 * @property blobPath the destination blob
 * @property blobSink the sink providing write access
 */
class BlobOutputFile(
    private val blobPath: BlobPath,
    private val blobSink: BlobSink
) : OutputFile {

    override fun create(blockSizeHint: Long): PositionOutputStream {
        val out = blobSink.openOutputStream(blobPath)
        return CountingPositionOutputStream(out)
    }

    override fun createOrOverwrite(blockSizeHint: Long): PositionOutputStream {
        val out = blobSink.openOutputStream(blobPath)
        return CountingPositionOutputStream(out)
    }

    override fun supportsBlockSize(): Boolean = false

    override fun defaultBlockSize(): Long = 0
}

/**
 * A [PositionOutputStream] that tracks the current write position
 * by counting bytes written to the delegate [OutputStream].
 */
internal class CountingPositionOutputStream(
    private val delegate: OutputStream
) : PositionOutputStream() {

    private var pos: Long = 0

    override fun getPos(): Long = pos

    override fun write(b: Int) {
        delegate.write(b)
        pos++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        delegate.write(b, off, len)
        pos += len
    }

    override fun flush() {
        delegate.flush()
    }

    override fun close() {
        delegate.close()
    }
}
