package io.qpointz.mill.cloud.aws.autoconfigure

import io.qpointz.mill.resource.MillConfigurationResourceKey
import org.springframework.core.io.AbstractResource
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.io.InputStream
import java.net.URI
import java.util.concurrent.atomic.AtomicReference

/**
 * Read-only Spring [org.springframework.core.io.Resource] for {@code s3://bucket/key} locations.
 */
internal class MillS3ObjectResource(
    private val location: String,
    private val properties: S3StorageProperties,
) : AbstractResource(), MillConfigurationResourceKey {

    private val bucket: String
    private val key: String
    private val clientRef = AtomicReference<S3Client?>()

    init {
        val uri = URI.create(location)
        bucket = uri.host ?: throw IllegalArgumentException("S3 location must include bucket: $location")
        val rawPath = uri.path?.trim('/') ?: ""
        require(rawPath.isNotEmpty()) { "S3 location must include object key: $location" }
        key = rawPath
    }

    private fun client(): S3Client =
        clientRef.updateAndGet { existing -> existing ?: buildClient() }!!

    private fun buildClient(): S3Client {
        val b = S3Client.builder()
        properties.region?.trim()?.takeIf { it.isNotEmpty() }?.let { b.region(Region.of(it)) }
        val access = properties.accessKey?.trim()?.takeIf { it.isNotEmpty() }
        val secret = properties.secretKey?.trim()?.takeIf { it.isNotEmpty() }
        if (access != null && secret != null) {
            b.credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(access, secret)),
            )
        } else {
            b.credentialsProvider(DefaultCredentialsProvider.create())
        }
        properties.endpoint?.trim()?.takeIf { it.isNotEmpty() }?.let {
            b.endpointOverride(URI.create(it))
            b.forcePathStyle(true)
        }
        return b.build()
    }

    override fun exists(): Boolean = try {
        client().headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build())
        true
    } catch (e: S3Exception) {
        if (e.statusCode() == 404) false else throw e
    }

    override fun getInputStream(): InputStream =
        client().getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())

    override fun getDescription(): String = "s3://$bucket/$key"

    override fun millConfigurationStableKey(): String {
        val base = "s3://$bucket/$key"
        val ep = properties.endpoint?.trim()?.takeIf { it.isNotEmpty() } ?: return base
        return try {
            val u = URI.create(ep)
            val host = u.host ?: ep
            val port = if (u.port > 0) ":${u.port}" else ""
            "$base|$host$port"
        } catch (_: Exception) {
            "$base|$ep"
        }
    }
}
