package io.qpointz.mill.cloud.aws.autoconfigure

import io.qpointz.mill.cloud.aws.blob.S3StorageFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ProtocolResolver
import software.amazon.awssdk.services.s3.S3Client

/**
 * Spring Boot autoconfiguration for the AWS S3 storage backend.
 *
 * Activates when:
 * - [S3Client] is on the classpath
 * - `mill.cloud.aws.s3.enabled` is `true` (the default)
 *
 * Registers an [S3StorageFactory] bean so that Spring-managed source
 * materializers can create S3-backed blob sources from descriptors.
 *
 * Registers a [ProtocolResolver] for {@code s3://} resource locations so flow descriptors and
 * metadata seeds can load YAML from S3-compatible endpoints.
 *
 * The [S3Client] itself is **not** created eagerly — it is built lazily when a resource is opened.
 */
@AutoConfiguration(
    beforeName = [
        "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration",
        "io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration",
    ],
)
@ConditionalOnClass(S3Client::class)
@ConditionalOnProperty(prefix = "mill.cloud.aws.s3", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(S3StorageProperties::class)
class S3AutoConfiguration {

    /**
     * Provides the [S3StorageFactory] bean for SPI-free Spring environments.
     *
     * @return a new [S3StorageFactory] instance
     */
    @Bean
    fun s3StorageFactory(): S3StorageFactory = S3StorageFactory()

    /**
     * Resolves {@code s3://bucket/key} locations through the AWS SDK using [S3StorageProperties].
     *
     * @param properties optional endpoint and region overrides
     * @return protocol resolver registered on the application [org.springframework.core.io.ResourceLoader]
     */
    @Bean
    fun millS3ProtocolResolver(properties: S3StorageProperties): ProtocolResolver =
        ProtocolResolver { location, _ ->
            if (location.startsWith("s3://")) {
                MillS3ObjectResource(location, properties)
            } else {
                null
            }
        }
}
