package io.qpointz.mill.source

import java.io.Closeable
import java.io.OutputStream

/**
 * Storage-agnostic write access to a blob store.
 *
 * This is the write counterpart to [BlobSource]. Implementations provide
 * blob creation for a specific storage backend (local filesystem, S3,
 * Azure Blob Storage, etc.).
 *
 * A [BlobSink] is [Closeable] — callers should use it within a
 * `use` block to ensure underlying resources are released.
 *
 * @see BlobSource
 * @see LocalBlobSink
 */
interface BlobSink : Closeable {

    /**
     * Opens a streaming write channel to create or overwrite a blob.
     *
     * @param path a blob handle identifying the destination
     * @return an [OutputStream] to write the blob content
     */
    fun openOutputStream(path: BlobPath): OutputStream
}
