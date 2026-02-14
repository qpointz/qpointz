package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import org.apache.parquet.io.DelegatingSeekableInputStream
import org.apache.parquet.io.InputFile
import org.apache.parquet.io.SeekableInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.SeekableByteChannel

/**
 * Storage-agnostic [InputFile] that reads via [BlobSource].
 *
 * Bridges the Mill storage abstraction to Parquet's I/O interface,
 * enabling Parquet reads from any backend (local, ADLS, S3, etc.).
 *
 * @property blobPath   the blob to read
 * @property blobSource the source providing I/O access
 */
class BlobInputFile(
    private val blobPath: BlobPath,
    private val blobSource: BlobSource
) : InputFile {

    override fun getLength(): Long {
        blobSource.openSeekableChannel(blobPath).use { channel ->
            return channel.size()
        }
    }

    override fun newStream(): SeekableInputStream {
        val channel = blobSource.openSeekableChannel(blobPath)
        return ChannelSeekableInputStream(channel)
    }
}

/**
 * Bridges a [SeekableByteChannel] to Parquet's [SeekableInputStream].
 *
 * Extends [DelegatingSeekableInputStream] with position and seek
 * support backed by the channel.
 */
internal class ChannelSeekableInputStream(
    private val channel: SeekableByteChannel
) : DelegatingSeekableInputStream(Channels.newInputStream(channel)) {

    override fun getPos(): Long = channel.position()

    override fun seek(newPos: Long) {
        channel.position(newPos)
    }

    override fun readFully(bytes: ByteArray) {
        val buf = ByteBuffer.wrap(bytes)
        while (buf.hasRemaining()) {
            val read = channel.read(buf)
            if (read < 0) throw java.io.EOFException("Reached end of channel")
        }
    }

    override fun readFully(buf: ByteBuffer) {
        while (buf.hasRemaining()) {
            val read = channel.read(buf)
            if (read < 0) throw java.io.EOFException("Reached end of channel")
        }
    }

    override fun close() {
        channel.close()
        super.close()
    }
}
