package io.qpointz.mill.cloud.gcp.autoconfigure

import com.google.cloud.storage.Storage
import io.qpointz.mill.cloud.gcp.blob.GcsStorageFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration that registers a [GcsStorageFactory] bean when the
 * Google Cloud Storage client is on the classpath and the feature is enabled.
 */
@AutoConfiguration
@ConditionalOnClass(Storage::class)
@ConditionalOnProperty(prefix = "mill.cloud.gcp.gcs", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GcsStorageProperties::class)
class GcsAutoConfiguration {

    /**
     * Registers the [GcsStorageFactory] as a Spring bean.
     *
     * The factory itself does not open any network connections — the GCS
     * client is created lazily at [GcsStorageFactory.create] time.
     */
    @Bean
    fun gcsStorageFactory(): GcsStorageFactory = GcsStorageFactory()
}
