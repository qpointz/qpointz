package io.qpointz.mill.data.backend.flow

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
import org.mockito.Mockito
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Minimal Spring context for flow metadata IT: file-backed metadata repository, flow backend, merged facets.
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
open class FlowFacetMetadataTestApplication {

    @Bean
    open fun substraitDispatcher(): SubstraitDispatcher = Mockito.mock(SubstraitDispatcher::class.java)
}
