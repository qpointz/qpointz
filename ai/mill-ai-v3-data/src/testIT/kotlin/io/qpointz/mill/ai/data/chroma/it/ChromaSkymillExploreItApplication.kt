package io.qpointz.mill.ai.data.chroma.it

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowDescriptorMetadataSourceAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.LogicalLayoutMetadataSourceAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.MetadataEntityUrnCodecAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.SchemaFacetServiceAutoConfiguration
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration
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
 * Spring slice for WI-171: Skymill flow **query execution** (not only SQL parse) plus service beans
 * from [DefaultServiceConfiguration] so [io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher]
 * is available.
 *
 * Intentionally **does not** register a standalone [io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher]
 * bean — the same bean comes from [DefaultServiceConfiguration], avoiding duplication with
 * [io.qpointz.mill.ai.data.sql.it.SqlValidatorSkymillFlowItApplication].
 *
 * Properties: profile `chroma-explore-skymill` in `application-chroma-explore-skymill.yml`.
 */
@SpringBootConfiguration
@Import(MetadataCoreConfiguration::class, DefaultServiceConfiguration::class)
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
open class ChromaSkymillExploreItApplication
