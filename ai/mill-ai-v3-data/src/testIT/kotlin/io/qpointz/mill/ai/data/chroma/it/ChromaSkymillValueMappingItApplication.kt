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
import io.qpointz.mill.ai.autoconfigure.AiV3JpaConfiguration
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.providers.AiProvidersAutoConfiguration
import io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Skymill flow + JPA (V6) + Mill AI embedding/sync beans for value-mapping Chroma IT.
 *
 * Properties: profiles `chroma-explore-skymill` and `chroma-value-mapping-it-infra` (see YAML).
 */
@SpringBootConfiguration
@Import(
    MetadataCoreConfiguration::class,
    DefaultServiceConfiguration::class,
    ChromaEmbeddingStoreItConfiguration::class,
)
@ImportAutoConfiguration(
    DataSourceAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class,
    FlywayAutoConfiguration::class,
    PersistenceAutoConfiguration::class,
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
    AiProvidersAutoConfiguration::class,
    EmbeddingAutoConfiguration::class,
    AiV3JpaConfiguration::class,
    ChromaSkymillValueMappingBeansConfiguration::class,
)
open class ChromaSkymillValueMappingItApplication
