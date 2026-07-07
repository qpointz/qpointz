package io.qpointz.mill.ai.mcp.transport.http

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowDescriptorMetadataSourceAutoConfiguration
import io.qpointz.mill.autoconfigure.data.query.QueryResultEngineAutoConfiguration
import io.qpointz.mill.autoconfigure.data.resource.BackendResourceLoaderAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.LogicalLayoutMetadataSourceAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.MetadataEntityUrnCodecAutoConfiguration
import io.qpointz.mill.autoconfigure.data.schema.SchemaFacetServiceAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.AiV3AutoConfiguration
import io.qpointz.mill.ai.autoconfigure.AiV3DataAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.AiV3SqlValidatorAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.sqlquery.AiV3SqlQueryExecutionAutoConfiguration
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration
import io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration
import io.qpointz.mill.metadata.configuration.MetadataEntityServiceAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataFileRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataImportExportAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataRepositoryAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Skymill flow + query-result engine + AI/MCP autoconfigure for sql-query execution HTTP MCP ITs.
 */
@SpringBootApplication
@Import(MetadataCoreConfiguration::class, DefaultServiceConfiguration::class)
@ImportAutoConfiguration(
    JacksonAutoConfiguration::class,
    SqlAutoConfiguration::class,
    BackendAutoConfiguration::class,
    BackendResourceLoaderAutoConfiguration::class,
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
    AiV3DataAutoConfiguration::class,
    AiV3SqlValidatorAutoConfiguration::class,
    AiV3SqlQueryExecutionAutoConfiguration::class,
    QueryResultEngineAutoConfiguration::class,
    AiV3AutoConfiguration::class,
    AiV3McpHttpAutoConfiguration::class,
)
class McpSkymillSqlQueryExecutionITApplication
