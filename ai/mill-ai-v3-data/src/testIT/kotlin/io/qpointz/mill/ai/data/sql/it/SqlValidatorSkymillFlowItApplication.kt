package io.qpointz.mill.ai.data.sql.it

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowDescriptorMetadataSourceAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.LogicalLayoutMetadataSourceAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.MetadataEntityUrnCodecAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.SchemaFacetServiceAutoConfiguration
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher
import io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration
import io.qpointz.mill.metadata.configuration.MetadataEntityServiceAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataFileRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataImportExportAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration
import io.substrait.extension.SimpleExtension
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Test slice for Skymill + flow: **all** `mill.*` / `spring.*` properties live in
 * `src/testIT/resources/application.yml` (same shape as `apps/mill-service/application.yml` skymill profile).
 *
 * This class only imports the auto-configurations needed for metadata + flow backend and registers
 * [SubstraitDispatcher] (not expressible in YAML).
 */
@SpringBootConfiguration
@Import(MetadataCoreConfiguration::class)
@ImportAutoConfiguration(
    JacksonAutoConfiguration::class,
    SqlAutoConfiguration::class,
    BackendAutoConfiguration::class,
    FlowBackendAutoConfiguration::class,
    FlowDescriptorMetadataSourceAutoConfiguration::class,
    LogicalLayoutMetadataSourceAutoConfiguration::class,
    MetadataEntityUrnCodecAutoConfiguration::class,
    MetadataFileRepositoryAutoConfiguration::class,
    MetadataRepositoryAutoConfiguration::class,
    MetadataImportExportAutoConfiguration::class,
    MetadataEntityServiceAutoConfiguration::class,
    MetadataSeedAutoConfiguration::class,
    SchemaFacetServiceAutoConfiguration::class,
)
open class SqlValidatorSkymillFlowItApplication {

    @Bean
    open fun substraitDispatcher(extensionCollection: SimpleExtension.ExtensionCollection): SubstraitDispatcher =
        SubstraitDispatcher(extensionCollection)
}
