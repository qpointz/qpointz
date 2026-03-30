package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration
import io.qpointz.mill.metadata.configuration.MetadataEntityServiceAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataFileRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataImportExportAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
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
    MetadataSeedAutoConfiguration::class
)
class SkyMillMetadataTestApplication
