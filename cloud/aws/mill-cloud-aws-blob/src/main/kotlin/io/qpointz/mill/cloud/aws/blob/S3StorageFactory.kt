package io.qpointz.mill.cloud.aws.blob

import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.factory.StorageFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

/**
 * [StorageFactory] that creates an [S3BlobSource] from an [S3StorageDescriptor].
 *
 * The S3 client is built lazily at [create] time — not during Spring
 * `ApplicationContext` refresh — to avoid cold-start overhead.
 *
 * Credential resolution:
 * - **Ambient**: when [S3AuthDescriptor] is null, all key fields blank,
 *   or [S3AuthDescriptor.preferAmbientCredentials] is `true` → AWS SDK
 *   default credential provider chain.
 * - **Delegated**: when both `accessKeyId` and `secretAccessKey` are
 *   non-blank and `preferAmbientCredentials` is `false` → static
 *   credentials (basic or session, depending on `sessionToken`).
 */
class S3StorageFactory : StorageFactory {

    override val descriptorType: Class<out StorageDescriptor>
        get() = S3StorageDescriptor::class.java

    /**
     * Builds an [S3Client] from the given [descriptor] and wraps it in
     * an [S3BlobSource].
     *
     * @param descriptor must be an [S3StorageDescriptor]
     * @return a ready-to-use [S3BlobSource]
     * @throws IllegalArgumentException if [descriptor] is not [S3StorageDescriptor]
     */
    override fun create(descriptor: StorageDescriptor): BlobSource {
        require(descriptor is S3StorageDescriptor) {
            "Expected S3StorageDescriptor, got ${descriptor::class.java.name}"
        }

        val builder = S3Client.builder()

        descriptor.region?.let { builder.region(Region.of(it)) }

        descriptor.endpoint?.let {
            builder.endpointOverride(URI.create(it))
            builder.forcePathStyle(true)
        }

        val auth = descriptor.auth
        if (auth != null && auth.useDelegatedCredentials) {
            val credentials = if (!auth.sessionToken.isNullOrBlank()) {
                AwsSessionCredentials.create(
                    auth.accessKeyId!!,
                    auth.secretAccessKey!!,
                    auth.sessionToken
                )
            } else {
                AwsBasicCredentials.create(
                    auth.accessKeyId!!,
                    auth.secretAccessKey!!
                )
            }
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials))
        }

        val client = builder.build()
        return S3BlobSource(client, descriptor.bucket, descriptor.prefix)
    }
}
