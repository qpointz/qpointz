package io.qpointz.mill.cloud.aws.blob

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.RequestPayer
import java.io.InputStream
import java.nio.channels.SeekableByteChannel

/**
 * A [BlobSource] backed by Amazon S3.
 *
 * Lists objects under the configured [prefix] in the given [bucket]
 * and provides streaming and seekable access via the AWS SDK v2.
 *
 * The caller must [close] this source when finished to release the
 * underlying [S3Client].
 *
 * @property client the S3 client (lifecycle owned by this source)
 * @property bucket the S3 bucket name
 * @property prefix key prefix for listing (empty string → entire bucket)
 * @property requesterPays when `true`, include `x-amz-request-payer: requester` on S3 reads
 *   (required for many Requester Pays buckets when the caller is not the bucket owner)
 */
class S3BlobSource(
    private val client: S3Client,
    private val bucket: String,
    private val prefix: String = "",
    private val requesterPays: Boolean = false
) : BlobSource {

    /**
     * Lists all objects under [prefix] in [bucket].
     *
     * Handles pagination transparently via continuation tokens.
     *
     * @return a lazily evaluated [Sequence] of [S3BlobPath] handles
     */
    override fun listBlobs(): Sequence<BlobPath> = sequence {
        var continuationToken: String? = null
        do {
            val requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
            if (requesterPays) {
                requestBuilder.requestPayer(RequestPayer.REQUESTER)
            }

            continuationToken?.let { requestBuilder.continuationToken(it) }

            val response = client.listObjectsV2(requestBuilder.build())
            for (obj in response.contents()) {
                yield(
                    S3BlobPath(
                        bucket = bucket,
                        key = obj.key(),
                        contentLength = obj.size()
                    )
                )
            }
            continuationToken = if (response.isTruncated == true) response.nextContinuationToken() else null
        } while (continuationToken != null)
    }

    /**
     * Opens a streaming [InputStream] for the given blob.
     *
     * The returned stream reads directly from the S3 response body
     * without buffering the entire object into heap memory.
     *
     * @param path a [BlobPath] (must be an [S3BlobPath])
     * @return a streaming [InputStream] — caller must close it
     */
    override fun openInputStream(path: BlobPath): InputStream {
        val s3Path = requireS3Path(path)
        val getBuilder = GetObjectRequest.builder()
            .bucket(s3Path.bucket)
            .key(s3Path.key)
        if (requesterPays) {
            getBuilder.requestPayer(RequestPayer.REQUESTER)
        }
        return client.getObject(getBuilder.build())
    }

    /**
     * Opens a [SeekableByteChannel] for random-access reading.
     *
     * The channel uses ranged GET requests with a bounded read-ahead buffer,
     * suitable for formats like Parquet that require seekable I/O.
     *
     * @param path a [BlobPath] (must be an [S3BlobPath])
     * @return an [S3SeekableByteChannel] — caller must close it
     */
    override fun openSeekableChannel(path: BlobPath): SeekableByteChannel {
        val s3Path = requireS3Path(path)
        return S3SeekableByteChannel(
            client = client,
            bucket = s3Path.bucket,
            key = s3Path.key,
            knownContentLength = s3Path.contentLength,
            requesterPays = requesterPays
        )
    }

    /**
     * Closes the underlying [S3Client], releasing network resources.
     */
    override fun close() {
        client.close()
    }

    private fun requireS3Path(path: BlobPath): S3BlobPath {
        require(path is S3BlobPath) {
            "Expected S3BlobPath, got ${path::class.java.name}"
        }
        return path
    }
}
