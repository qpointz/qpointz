package io.qpointz.mill.cloud.aws.autoconfigure

import io.qpointz.mill.cloud.aws.blob.S3StorageFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
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
 * The [S3Client] itself is **not** created eagerly — it is built
 * lazily inside [S3StorageFactory.create] to avoid cold-start overhead
 * during application context refresh.
 */
@AutoConfiguration
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
}
