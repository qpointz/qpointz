package io.qpointz.mill.cloud.azure.blob

import com.azure.core.credential.AzureSasCredential
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.factory.StorageFactory

/**
 * [StorageFactory] for Azure Data Lake Storage / Blob Storage.
 *
 * Builds a `BlobServiceClient` at [create] time (cold-start safe — no client
 * is constructed during Spring context refresh). Resolution order:
 *
 * 1. **Connection string** — non-blank [AdlsStorageDescriptor.connectionString]
 * 2. **Ambient (forced)** — [AdlsAuthDescriptor.preferAmbientCredentials] with [AdlsStorageDescriptor.endpoint]
 * 3. **SAS** — [AdlsAuthDescriptor.sasToken] and/or SAS query on [AdlsStorageDescriptor.endpoint]
 * 4. **Shared key** — [AdlsAuthDescriptor.accountKey] (and optional [AdlsAuthDescriptor.accountName], else derived from the endpoint host)
 * 5. **Default** — [com.azure.identity.DefaultAzureCredential] with [AdlsStorageDescriptor.endpoint]
 */
class AdlsStorageFactory : StorageFactory {

    override val descriptorType: Class<out StorageDescriptor>
        get() = AdlsStorageDescriptor::class.java

    override fun create(descriptor: StorageDescriptor): BlobSource {
        require(descriptor is AdlsStorageDescriptor) {
            "Expected AdlsStorageDescriptor, got ${descriptor::class.java.name}"
        }

        val auth = descriptor.auth
        val effectiveCs = descriptor.connectionString?.takeIf { it.isNotBlank() }
        val builder = BlobServiceClientBuilder()

        when {
            !effectiveCs.isNullOrBlank() -> {
                builder.connectionString(effectiveCs)
            }

            auth != null && auth.preferAmbientCredentials -> {
                builder.endpoint(requiredBlobServiceUrl(descriptor))
                builder.credential(DefaultAzureCredentialBuilder().build())
            }

            auth != null && !auth.sasToken.isNullOrBlank() -> {
                val resolved = AdlsBlobEndpointSupport.resolveServiceUrlAndSas(
                    requiredBlobServiceUrl(descriptor),
                    auth.sasToken
                ).getOrThrow()
                builder.endpoint(resolved.first)
                builder.credential(AzureSasCredential(resolved.second!!))
            }

            else -> {
                val resolved = AdlsBlobEndpointSupport.resolveServiceUrlAndSas(
                    requiredBlobServiceUrl(descriptor),
                    null
                ).getOrThrow()
                when {
                    resolved.second != null -> {
                        builder.endpoint(resolved.first)
                        builder.credential(AzureSasCredential(resolved.second))
                    }

                    auth != null && !auth.accountKey.isNullOrBlank() -> {
                        val accountName = auth.accountName?.takeIf { it.isNotBlank() }
                            ?: AdlsBlobEndpointSupport.storageAccountFromBlobOrDfsHost(descriptor.endpoint.trim())
                            ?: error(
                                "Shared-key auth requires auth.accountName or an endpoint host like " +
                                    "https://{account}.blob.core.windows.net"
                            )
                        builder.endpoint(resolved.first)
                        builder.credential(StorageSharedKeyCredential(accountName, auth.accountKey!!))
                    }

                    else -> {
                        builder.endpoint(resolved.first)
                        builder.credential(DefaultAzureCredentialBuilder().build())
                    }
                }
            }
        }

        val serviceClient = builder.buildClient()
        val containerClient = serviceClient.getBlobContainerClient(descriptor.container)
        return AdlsBlobSource(containerClient, descriptor.prefix)
    }

    private fun requiredBlobServiceUrl(descriptor: AdlsStorageDescriptor): String {
        val url = descriptor.endpoint.trim()
        require(url.isNotBlank()) {
            "Non-blank storage.endpoint is required unless storage.connectionString is set"
        }
        return url
    }
}
