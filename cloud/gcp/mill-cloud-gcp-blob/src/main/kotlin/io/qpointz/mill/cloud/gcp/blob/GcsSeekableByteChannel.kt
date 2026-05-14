package io.qpointz.mill.cloud.gcp.blob

import com.google.cloud.ReadChannel
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel

/**
 * Read-only [SeekableByteChannel] backed by a GCS [ReadChannel].
 *
 * Seeking is delegated to [ReadChannel.seek], which issues range-read
 * RPCs on the next read after a seek. The channel reports a fixed [size]
 * obtained from the blob metadata at construction time.
 *
 * @param readChannel the underlying GCS read channel
 * @param blobSize    total size of the blob in bytes
 */
class GcsSeekableByteChannel(
    private val readChannel: ReadChannel,
    private val blobSize: Long
) : SeekableByteChannel {

    private var position: Long = 0L
    private var open: Boolean = true

    override fun read(dst: ByteBuffer): Int {
        ensureOpen()
        if (position >= blobSize) return -1
        readChannel.seek(position)
        val bytesRead = readChannel.read(dst)
        if (bytesRead > 0) {
            position += bytesRead
        }
        return bytesRead
    }

    override fun write(src: ByteBuffer): Int {
        throw NonWritableChannelException()
    }

    override fun position(): Long {
        ensureOpen()
        return position
    }

    override fun position(newPosition: Long): SeekableByteChannel {
        ensureOpen()
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
        if (open) {
            open = false
            readChannel.close()
        }
    }

    private fun ensureOpen() {
        if (!open) throw ClosedChannelException()
    }
}
