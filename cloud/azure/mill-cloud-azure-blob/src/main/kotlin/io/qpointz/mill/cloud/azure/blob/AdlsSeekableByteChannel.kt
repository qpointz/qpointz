package io.qpointz.mill.cloud.azure.blob

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.models.BlobRange
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel

/**
 * Read-only [SeekableByteChannel] over an Azure blob using ranged downloads.
 *
 * Each [read] call issues a `BlobClient.downloadWithResponse` with a
 * [BlobRange] covering `[position, position + min(remaining, MAX_CHUNK))`.
 * Blob size is lazily fetched once via `getProperties().getBlobSize()`.
 *
 * This channel does **not** cache any data beyond the current read request,
 * making it suitable for Parquet/Avro footer reads where only small ranges
 * are accessed.
 *
 * @property blobClient Azure blob client for the target blob
 */
class AdlsSeekableByteChannel(
    private val blobClient: BlobClient
) : SeekableByteChannel {

    private var position: Long = 0
    private var open: Boolean = true
    private val blobSize: Long by lazy { blobClient.properties.blobSize }

    override fun read(dst: ByteBuffer): Int {
        ensureOpen()
        if (position >= blobSize) return -1

        val remaining = dst.remaining().toLong()
        val toRead = minOf(remaining, blobSize - position, MAX_CHUNK)
        if (toRead <= 0) return -1

        val range = BlobRange(position, toRead)
        val buffer = ByteArrayOutputStream(toRead.toInt())
        blobClient.downloadStreamWithResponse(buffer, range, null, null, false, null, null)

        val bytes = buffer.toByteArray()
        dst.put(bytes)
        position += bytes.size
        return bytes.size
    }

    override fun write(src: ByteBuffer): Int {
        throw NonWritableChannelException()
    }

    override fun position(): Long = position

    override fun position(newPosition: Long): SeekableByteChannel {
        require(newPosition >= 0) { "Position must be non-negative, got $newPosition" }
        position = newPosition
        return this
    }

    override fun size(): Long {
        ensureOpen()
        return blobSize
    }

    override fun truncate(size: Long): SeekableByteChannel {
        throw NonWritableChannelException()
    }

    override fun isOpen(): Boolean = open

    override fun close() {
        open = false
    }

    private fun ensureOpen() {
        if (!open) throw ClosedChannelException()
    }

    companion object {
        /** Maximum bytes per range-read request (4 MB). */
        private const val MAX_CHUNK = 4L * 1024 * 1024
    }
}
