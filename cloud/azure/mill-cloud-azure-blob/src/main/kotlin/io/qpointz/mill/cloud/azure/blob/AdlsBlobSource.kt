package io.qpointz.mill.cloud.azure.blob

import com.azure.storage.blob.BlobContainerClient
import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import java.io.InputStream
import java.net.URI
import java.nio.channels.SeekableByteChannel

/**
 * [BlobSource] backed by an Azure Blob Storage container.
 *
 * Blob discovery is performed via [BlobContainerClient.listBlobs]; streaming reads
 * use [com.azure.storage.blob.BlobClient.openInputStream] and seekable reads delegate
 * to [AdlsSeekableByteChannel] (range-based downloads).
 *
 * Azure SDK HTTP clients are self-managed — [close] is a no-op.
 *
 * @property containerClient pre-built Azure container client
 * @property prefix          optional blob name prefix filter
 */
class AdlsBlobSource(
    private val containerClient: BlobContainerClient,
    private val prefix: String?
) : BlobSource {

    override fun listBlobs(): Sequence<BlobPath> {
        val listing = if (prefix.isNullOrBlank()) {
            containerClient.listBlobs()
        } else {
            containerClient.listBlobsByHierarchy(prefix)
        }
        val baseUri = containerClient.blobContainerUrl
        return listing.asSequence()
            .filter { it.isPrefix == null || !it.isPrefix }
            .map { item ->
                val name = item.name
                AdlsBlobPath(
                    blobName = name,
                    uri = URI.create("$baseUri/$name")
                )
            }
    }

    override fun openInputStream(path: BlobPath): InputStream {
        val adlsPath = requireAdlsPath(path)
        return containerClient.getBlobClient(adlsPath.blobName).openInputStream()
    }

    override fun openSeekableChannel(path: BlobPath): SeekableByteChannel {
        val adlsPath = requireAdlsPath(path)
        val blobClient = containerClient.getBlobClient(adlsPath.blobName)
        return AdlsSeekableByteChannel(blobClient)
    }

    override fun close() {
        // Azure SDK HTTP clients are self-managed; no explicit teardown needed.
    }

    private fun requireAdlsPath(path: BlobPath): AdlsBlobPath {
        require(path is AdlsBlobPath) {
            "Expected AdlsBlobPath, got ${path::class.java.name}"
        }
        return path
    }
}
