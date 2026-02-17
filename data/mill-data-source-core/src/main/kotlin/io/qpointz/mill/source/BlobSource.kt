package io.qpointz.mill.source

import java.io.Closeable
import java.io.InputStream
import java.nio.channels.SeekableByteChannel

/**
 * Storage-agnostic access to a collection of blobs.
 *
 * Implementations provide blob discovery ([listBlobs]) and I/O
 * ([openInputStream], [openSeekableChannel]) for a specific storage
 * backend (local filesystem, S3, Azure Blob Storage, etc.).
 *
 * A [BlobSource] is [Closeable] — callers should use it within a
 * `use` block to ensure underlying resources are released.
 *
 * @see BlobPath
 * @see LocalBlobSource
 */
interface BlobSource : Closeable {

    /**
     * Discovers all blobs available in this source.
     *
     * @return a [Sequence] of [BlobPath] handles, lazily evaluated
     */
    fun listBlobs(): Sequence<BlobPath>

    /**
     * Opens a streaming read channel for the given [path].
     *
     * @param path a blob handle obtained from [listBlobs]
     * @return an [InputStream] to read the blob content
     */
    fun openInputStream(path: BlobPath): InputStream

    /**
     * Opens a random-access read channel for the given [path].
     *
     * Required by formats like Parquet that need seekable access.
     *
     * @param path a blob handle obtained from [listBlobs]
     * @return a [SeekableByteChannel] for random-access reads
     */
    fun openSeekableChannel(path: BlobPath): SeekableByteChannel
}
