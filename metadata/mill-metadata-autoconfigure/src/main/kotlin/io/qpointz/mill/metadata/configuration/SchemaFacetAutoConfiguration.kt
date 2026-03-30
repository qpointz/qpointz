package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacetServiceImpl
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Auto-configures [SchemaFacetService] once physical [SchemaProvider] and greenfield metadata
 * repositories ([MetadataEntityRepository], [FacetRepository]) are available.
 *
 * Facet type definitions are loaded **only** via `mill.metadata.seed.resources` (not this module).
 */
@AutoConfiguration
@AutoConfigureAfter(
    MetadataRepositoryAutoConfiguration::class,
    MetadataJpaPersistenceAutoConfiguration::class,
    MetadataCoreConfiguration::class
)
@ConditionalOnClass(SchemaFacetService::class)
class SchemaFacetAutoConfiguration {

    /**
     * @param schemaProvider physical schema source
     * @param entityRepository `metadata_entity` rows
     * @param facetRepository `metadata_entity_facet` rows
     * @return schema / metadata merge service
     */
    @Bean
    @ConditionalOnMissingBean(SchemaFacetService::class)
    @ConditionalOnBean(SchemaProvider::class, MetadataEntityRepository::class, FacetRepository::class)
    fun schemaFacetService(
        schemaProvider: SchemaProvider,
        entityRepository: MetadataEntityRepository,
        facetRepository: FacetRepository
    ): SchemaFacetService = SchemaFacetServiceImpl(schemaProvider, entityRepository, facetRepository)
}
