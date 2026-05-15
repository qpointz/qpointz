package io.qpointz.mill.cloud.azure.autoconfigure

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import io.qpointz.mill.resource.MillConfigurationResourceKey
import org.springframework.core.io.AbstractResource
import java.io.InputStream
import java.net.URI
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

/**
 * Read-only Spring [org.springframework.core.io.Resource] for {@code azure-blob://container/blob} locations.
 */
internal class MillAzureBlobObjectResource(
    private val location: String,
    private val properties: AdlsStorageProperties,
) : AbstractResource(), MillConfigurationResourceKey {

    private val container: String
    private val blobName: String
    private val clientRef = AtomicReference<BlobServiceClient?>()

    init {
        val uri = URI.create(location)
        container = uri.host ?: throw IllegalArgumentException("Azure blob location must include container: $location")
        val raw = uri.path?.trimStart('/') ?: ""
        require(raw.isNotEmpty()) { "Azure blob location must include blob path: $location" }
        blobName = raw
    }

    private fun client(): BlobServiceClient = clientRef.updateAndGet { existing -> existing ?: buildClient() }!!

    private fun buildClient(): BlobServiceClient {
        val cs = properties.connectionString?.trim()?.takeIf { it.isNotEmpty() }
        if (cs != null) {
            return BlobServiceClientBuilder().connectionString(cs).buildClient()
        }
        val ep = properties.blobServiceEndpoint?.trim()?.takeIf { it.isNotEmpty() }
            ?: error(
                "mill.cloud.azure.adls.connection-string or mill.cloud.azure.adls.blob-service-endpoint " +
                    "is required to resolve azure-blob:// resources",
            )
        return BlobServiceClientBuilder()
            .endpoint(ep)
            .credential(DefaultAzureCredentialBuilder().build())
            .buildClient()
    }

    override fun exists(): Boolean =
        client().getBlobContainerClient(container).getBlobClient(blobName).exists()

    override fun getInputStream(): InputStream =
        client().getBlobContainerClient(container).getBlobClient(blobName).openInputStream()

    override fun getDescription(): String = "azure-blob://$container/$blobName"

    override fun millConfigurationStableKey(): String {
        val base = "azure-blob://$container/$blobName"
        val cs = properties.connectionString?.trim()?.takeIf { it.isNotEmpty() }
        if (cs != null) {
            val account = ACCOUNT_NAME.matcher(cs).let { m -> if (m.find()) m.group(1).trim() else null }
            if (!account.isNullOrEmpty()) {
                val blobEndpoint = BLOB_ENDPOINT.matcher(cs).let { m -> if (m.find()) m.group(1).trim() else null }
                if (!blobEndpoint.isNullOrEmpty()) {
                    return try {
                        val host = URI.create(blobEndpoint).host ?: blobEndpoint
                        "$base|$account|$host"
                    } catch (_: Exception) {
                        "$base|$account"
                    }
                }
                return "$base|$account"
            }
        }
        val ep = properties.blobServiceEndpoint?.trim()?.takeIf { it.isNotEmpty() }
        if (ep != null) {
            return try {
                val u = URI.create(ep)
                val host = u.host ?: ep
                val port = if (u.port > 0) ":${u.port}" else ""
                "$base|$host$port"
            } catch (_: Exception) {
                "$base|$ep"
            }
        }
        return base
    }

    private companion object {
        private val ACCOUNT_NAME: Pattern = Pattern.compile("AccountName=([^;]+)", Pattern.CASE_INSENSITIVE)
        private val BLOB_ENDPOINT: Pattern = Pattern.compile("BlobEndpoint=([^;]+)", Pattern.CASE_INSENSITIVE)
    }
}
