package io.qpointz.mill.cloud.gcp.blob

import io.qpointz.mill.source.BlobPath
import java.net.URI

/**
 * A [BlobPath] identifying an object in Google Cloud Storage.
 *
 * The [uri] follows the `gs://bucket/name` scheme.
 *
 * @property bucket GCS bucket name
 * @property name   full object key within the bucket
 */
data class GcsBlobPath(
    val bucket: String,
    val name: String
) : BlobPath {

    /** URI in `gs://bucket/name` form. */
    override val uri: URI = URI.create("gs://$bucket/$name")
}
