package io.qpointz.mill.cloud.gcp.autoconfigure

import com.google.cloud.NoCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.StorageOptions
import io.qpointz.mill.resource.MillConfigurationResourceKey
import org.springframework.core.io.AbstractResource
import java.io.InputStream
import java.net.URI
import java.nio.channels.Channels
import java.util.concurrent.atomic.AtomicReference

/**
 * Read-only Spring [org.springframework.core.io.Resource] for {@code gs://bucket/object} locations.
 */
internal class MillGcsObjectResource(
    private val location: String,
    private val properties: GcsStorageProperties,
) : AbstractResource(), MillConfigurationResourceKey {

    private val bucket: String
    private val objectName: String
    private val storageRef = AtomicReference<Storage?>()

    init {
        val uri = URI.create(location)
        bucket = uri.host ?: throw IllegalArgumentException("GCS location must include bucket: $location")
        val raw = uri.path?.trimStart('/') ?: ""
        require(raw.isNotEmpty()) { "GCS location must include object name: $location" }
        objectName = raw
    }

    private fun storage(): Storage = storageRef.updateAndGet { existing -> existing ?: buildStorage() }!!

    private fun buildStorage(): Storage {
        val builder = StorageOptions.newBuilder()
        properties.projectId?.trim()?.takeIf { it.isNotEmpty() }?.let { builder.setProjectId(it) }
        properties.emulatorHost?.trim()?.takeIf { it.isNotEmpty() }?.let {
            builder.setHost(it)
            builder.setCredentials(NoCredentials.getInstance())
        }
        return builder.build().service
    }

    override fun exists(): Boolean =
        try {
            storage().get(BlobId.of(bucket, objectName)) != null
        } catch (e: StorageException) {
            if (e.code == 404) {
                false
            } else {
                throw e
            }
        }

    override fun getInputStream(): InputStream {
        val reader = storage().reader(BlobId.of(bucket, objectName))
        return Channels.newInputStream(reader)
    }

    override fun getDescription(): String = "gs://$bucket/$objectName"

    override fun millConfigurationStableKey(): String {
        val base = "gs://$bucket/$objectName"
        val host = properties.emulatorHost?.trim()?.takeIf { it.isNotEmpty() } ?: return base
        return try {
            val u = URI.create(host)
            val h = u.host ?: host
            val port = if (u.port > 0) ":${u.port}" else ""
            "$base|$h$port"
        } catch (_: Exception) {
            "$base|$host"
        }
    }
}
