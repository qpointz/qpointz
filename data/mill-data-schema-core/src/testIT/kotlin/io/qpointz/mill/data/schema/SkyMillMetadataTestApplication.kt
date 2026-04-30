package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration
import io.qpointz.mill.metadata.configuration.MetadataEntityServiceAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataFileRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataImportExportAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Minimal Spring context for SkyMill metadata IT: file-backed metadata repository + seed pipeline.
 */
@SpringBootConfiguration
@Import(MetadataCoreConfiguration::class)
@ImportAutoConfiguration(
    JacksonAutoConfiguration::class,
    MetadataFileRepositoryAutoConfiguration::class,
    MetadataRepositoryAutoConfiguration::class,
    MetadataImportExportAutoConfiguration::class,
    MetadataEntityServiceAutoConfiguration::class,
    MetadataSeedAutoConfiguration::class,
)
// Spring configuration classes must be non-final for CGLIB proxying of @Bean methods.
open class SkyMillMetadataTestApplication {

    @Bean
    open fun metadataEntityUrnCodec(): MetadataEntityUrnCodec = DefaultMetadataEntityUrnCodec()
}
