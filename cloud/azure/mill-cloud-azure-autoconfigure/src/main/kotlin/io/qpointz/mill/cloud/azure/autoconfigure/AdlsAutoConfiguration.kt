package io.qpointz.mill.cloud.azure.autoconfigure

import com.azure.storage.blob.BlobServiceClient
import io.qpointz.mill.cloud.azure.blob.AdlsStorageFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ProtocolResolver

/**
 * Spring Boot auto-configuration for ADLS / Azure Blob Storage.
 *
 * Registers an [AdlsStorageFactory] bean when:
 * 1. [BlobServiceClient] is on the classpath
 * 2. `mill.cloud.azure.adls.enabled` is `true` (default)
 *
 * The factory itself does **not** build a `BlobServiceClient` at bean-creation
 * time — client construction is deferred to [AdlsStorageFactory.create] (cold-start safe).
 *
 * Registers a [ProtocolResolver] for {@code azure-blob://} resource locations so flow descriptors
 * and metadata seeds can load YAML from Azure Blob Storage or Azurite.
 */
@AutoConfiguration(
    beforeName = [
        "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration",
        "io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration",
    ],
)
@ConditionalOnClass(BlobServiceClient::class)
@ConditionalOnProperty(prefix = "mill.cloud.azure.adls", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AdlsStorageProperties::class)
class AdlsAutoConfiguration {

    /**
     * Registers the ADLS [AdlsStorageFactory] bean for the source materializer.
     *
     * @return an [AdlsStorageFactory] instance
     */
    @Bean
    fun adlsStorageFactory(): AdlsStorageFactory = AdlsStorageFactory()

    /**
     * Resolves {@code azure-blob://container/blob} locations through the Azure Blob SDK using
     * [AdlsStorageProperties].
     *
     * @param properties connection string or service endpoint configuration
     * @return protocol resolver registered on the application [org.springframework.core.io.ResourceLoader]
     */
    @Bean
    fun millAzureBlobProtocolResolver(properties: AdlsStorageProperties): ProtocolResolver =
        ProtocolResolver { location, _ ->
            if (location.startsWith("azure-blob://")) {
                MillAzureBlobObjectResource(location, properties)
            } else {
                null
            }
        }
}
