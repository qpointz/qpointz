package io.qpointz.mill.cloud.azure.autoconfigure

import com.azure.storage.blob.BlobServiceClient
import io.qpointz.mill.cloud.azure.blob.AdlsStorageFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Spring Boot auto-configuration for ADLS / Azure Blob Storage.
 *
 * Registers an [AdlsStorageFactory] bean when:
 * 1. [BlobServiceClient] is on the classpath
 * 2. `mill.cloud.azure.adls.enabled` is `true` (default)
 *
 * The factory itself does **not** build a `BlobServiceClient` at bean-creation
 * time — client construction is deferred to [AdlsStorageFactory.create] (cold-start safe).
 */
@AutoConfiguration
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
}
