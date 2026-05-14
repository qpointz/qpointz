package io.qpointz.mill.cloud.aws.blob

import io.qpointz.mill.source.BlobPath
import java.net.URI

/**
 * A [BlobPath] identifying an object in Amazon S3.
 *
 * The [uri] follows the `s3://bucket/key` convention.
 *
 * @property bucket the S3 bucket name
 * @property key    the full object key
 * @property uri    canonical `s3://` URI for this blob
 * @property contentLength object size in bytes when known (for example from [ListObjectsV2]);
 *   when non-null, readers may skip a separate `HeadObject` call to obtain length
 */
data class S3BlobPath(
    val bucket: String,
    val key: String,
    override val uri: URI = URI.create("s3://$bucket/$key"),
    val contentLength: Long? = null
) : BlobPath
