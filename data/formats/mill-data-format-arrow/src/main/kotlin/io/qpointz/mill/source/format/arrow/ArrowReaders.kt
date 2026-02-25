package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import org.apache.arrow.memory.BufferAllocator
import org.apache.arrow.vector.VectorSchemaRoot
import org.apache.arrow.vector.ipc.ArrowFileReader
import org.apache.arrow.vector.ipc.ArrowStreamReader
import org.apache.arrow.vector.ipc.SeekableReadChannel
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.channels.SeekableByteChannel

internal interface ArrowBatchReader : AutoCloseable {
    val root: VectorSchemaRoot
    fun loadNextBatch(): Boolean
}

internal object ArrowReaders {
    fun open(blob: BlobPath, blobSource: BlobSource, allocator: BufferAllocator): ArrowBatchReader {
        runCatching {
            val channel = blobSource.openSeekableChannel(blob)
            if (isArrowFile(channel)) {
                val reader = ArrowFileReader(SeekableReadChannel(channel), allocator)
                return FileBatchReader(reader, channel)
            }
            channel.close()
        }

        val stream = blobSource.openInputStream(blob)
        val streamReader = ArrowStreamReader(stream, allocator)
        return StreamBatchReader(streamReader, stream)
    }

    private fun isArrowFile(channel: SeekableByteChannel): Boolean {
        return try {
            val originalPosition = channel.position()
            channel.position(0)
            val magicBuffer = ByteBuffer.allocate(6)
            channel.read(magicBuffer)
            magicBuffer.flip()
            val magic = StandardCharsets.US_ASCII.decode(magicBuffer).toString()
            channel.position(originalPosition)
            magic == "ARROW1"
        } catch (_: Exception) {
            false
        }
    }

    private class FileBatchReader(
        private val reader: ArrowFileReader,
        private val channel: SeekableByteChannel
    ) : ArrowBatchReader {
        override val root: VectorSchemaRoot
            get() = reader.vectorSchemaRoot

        override fun loadNextBatch(): Boolean = reader.loadNextBatch()

        override fun close() {
            runCatching { reader.close() }
            runCatching { channel.close() }
        }
    }

    private class StreamBatchReader(
        private val reader: ArrowStreamReader,
        private val stream: InputStream
    ) : ArrowBatchReader {
        override val root: VectorSchemaRoot
            get() = reader.vectorSchemaRoot

        override fun loadNextBatch(): Boolean = reader.loadNextBatch()

        override fun close() {
            runCatching { reader.close() }
            runCatching { stream.close() }
        }
    }
}
