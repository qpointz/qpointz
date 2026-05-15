package io.qpointz.mill.cloud.gcp.autoconfigure

import com.google.cloud.storage.Storage
import io.qpointz.mill.cloud.gcp.blob.GcsStorageFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ProtocolResolver

/**
 * Auto-configuration that registers a [GcsStorageFactory] bean when the
 * Google Cloud Storage client is on the classpath and the feature is enabled.
 *
 * Also registers a [ProtocolResolver] for {@code gs://} locations so flow descriptors and metadata
 * seeds can load YAML from Google Cloud Storage or emulators.
 */
@AutoConfiguration(
    beforeName = [
        "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration",
        "io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration",
    ],
)
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

    /**
     * Resolves {@code gs://bucket/object} locations through the Google Cloud Storage client.
     *
     * @param properties emulator host and optional project id
     * @return protocol resolver registered on the application [org.springframework.core.io.ResourceLoader]
     */
    @Bean
    fun millGcsProtocolResolver(properties: GcsStorageProperties): ProtocolResolver =
        ProtocolResolver { location, _ ->
            if (location.startsWith("gs://")) {
                MillGcsObjectResource(location, properties)
            } else {
                null
            }
        }
}
