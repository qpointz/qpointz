package io.qpointz.mill.cloud.gcp.blob

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import java.io.InputStream
import java.nio.channels.Channels
import java.nio.channels.SeekableByteChannel

/**
 * [BlobSource] backed by Google Cloud Storage.
 *
 * Lists objects under the configured [prefix] within [bucket] and provides
 * streaming ([openInputStream]) and seekable ([openSeekableChannel]) access.
 *
 * The caller must [close] this source when finished to release the
 * underlying [Storage] client and HTTP transport.
 *
 * @property storage the GCS client
 * @property bucket  target bucket name
 * @property prefix  optional key prefix to scope listing
 */
class GcsBlobSource(
    private val storage: Storage,
    private val bucket: String,
    private val prefix: String?
) : BlobSource {

    override fun listBlobs(): Sequence<BlobPath> {
        val options = buildList {
            if (!prefix.isNullOrBlank()) {
                add(Storage.BlobListOption.prefix(prefix))
            }
        }
        return storage.list(bucket, *options.toTypedArray())
            .iterateAll()
            .asSequence()
            .map { blob -> GcsBlobPath(bucket = blob.bucket, name = blob.name) }
    }

    override fun openInputStream(path: BlobPath): InputStream {
        val gcsPath = requireGcsPath(path)
        val blobId = BlobId.of(gcsPath.bucket, gcsPath.name)
        val reader = storage.reader(blobId)
        return Channels.newInputStream(reader)
    }

    override fun openSeekableChannel(path: BlobPath): SeekableByteChannel {
        val gcsPath = requireGcsPath(path)
        val blobId = BlobId.of(gcsPath.bucket, gcsPath.name)
        val blob = storage.get(blobId)
            ?: throw IllegalStateException("Blob not found: ${gcsPath.uri}")
        val reader = storage.reader(blobId)
        return GcsSeekableByteChannel(reader, blob.size)
    }

    override fun close() {
        try {
            storage.close()
        } catch (_: Exception) {
            // best-effort cleanup
        }
    }

    private fun requireGcsPath(path: BlobPath): GcsBlobPath {
        require(path is GcsBlobPath) {
            "Expected GcsBlobPath, got ${path::class.java.name}"
        }
        return path
    }
}
