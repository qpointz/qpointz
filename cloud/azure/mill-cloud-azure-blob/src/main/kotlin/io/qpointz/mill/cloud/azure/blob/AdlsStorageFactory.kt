package io.qpointz.mill.cloud.azure.blob

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
 * is constructed during Spring context refresh). Authentication is selected
 * based on the [AdlsAuthDescriptor] inside the [AdlsStorageDescriptor]:
 *
 * 1. **Connection string** — `connectionString` is set
 * 2. **Shared key** — `accountName` + `accountKey` pair
 * 3. **Ambient** — all auth fields blank or `preferAmbientCredentials = true` →
 *    `DefaultAzureCredential`
 */
class AdlsStorageFactory : StorageFactory {

    override val descriptorType: Class<out StorageDescriptor>
        get() = AdlsStorageDescriptor::class.java

    override fun create(descriptor: StorageDescriptor): BlobSource {
        require(descriptor is AdlsStorageDescriptor) {
            "Expected AdlsStorageDescriptor, got ${descriptor::class.java.name}"
        }

        val auth = descriptor.auth
        val builder = BlobServiceClientBuilder()

        when {
            auth != null && auth.preferAmbientCredentials -> {
                builder.credential(DefaultAzureCredentialBuilder().build())
                builder.endpoint(descriptor.endpoint ?: descriptor.accountUrl)
            }

            auth != null && !auth.connectionString.isNullOrBlank() -> {
                builder.connectionString(auth.connectionString)
            }

            auth != null && !auth.accountName.isNullOrBlank() && !auth.accountKey.isNullOrBlank() -> {
                val cred = StorageSharedKeyCredential(auth.accountName, auth.accountKey)
                builder.credential(cred)
                builder.endpoint(descriptor.endpoint ?: descriptor.accountUrl)
            }

            else -> {
                builder.credential(DefaultAzureCredentialBuilder().build())
                builder.endpoint(descriptor.endpoint ?: descriptor.accountUrl)
            }
        }

        val serviceClient = builder.buildClient()
        val containerClient = serviceClient.getBlobContainerClient(descriptor.filesystem)
        return AdlsBlobSource(containerClient, descriptor.prefix)
    }
}
