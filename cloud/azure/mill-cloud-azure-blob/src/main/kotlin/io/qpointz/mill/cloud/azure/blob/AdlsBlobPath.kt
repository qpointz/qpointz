package io.qpointz.mill.cloud.azure.blob

import io.qpointz.mill.source.BlobPath
import java.net.URI

/**
 * [BlobPath] for Azure Blob Storage blobs.
 *
 * The [uri] follows the pattern
 * `https://{account}.blob.core.windows.net/{container}/{blobName}`.
 *
 * @property blobName the full blob name (key) within the container
 * @property uri      absolute HTTPS URI identifying this blob
 */
data class AdlsBlobPath(
    val blobName: String,
    override val uri: URI
) : BlobPath
