package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacetServiceImpl
import io.qpointz.mill.metadata.repository.MetadataRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Auto-configures [SchemaFacetService] using whichever [MetadataRepository] implementation is
 * active in the metadata layer.
 *
 * This wiring belongs in metadata autoconfigure because metadata persistence selection (JPA/file/
 * NoOp) is resolved here, while the schema service remains backend-agnostic and consumes only
 * [SchemaProvider] and [MetadataRepository].
 */
@AutoConfiguration
@AutoConfigureAfter(
    MetadataRepositoryAutoConfiguration::class,
    MetadataJpaPersistenceAutoConfiguration::class
)
@ConditionalOnClass(SchemaFacetService::class)
class SchemaFacetAutoConfiguration {

    /**
     * Creates [SchemaFacetService] when schema provider and metadata repository beans are present.
     *
     * @param schemaProvider active schema provider for physical model discovery
     * @param metadataRepository active metadata repository implementation
     * @return schema facet service implementation
     */
    @Bean
    @ConditionalOnMissingBean(SchemaFacetService::class)
    @ConditionalOnBean(SchemaProvider::class, MetadataRepository::class)
    fun schemaFacetService(
        schemaProvider: SchemaProvider,
        metadataRepository: MetadataRepository
    ): SchemaFacetService = SchemaFacetServiceImpl(schemaProvider, metadataRepository)
}
