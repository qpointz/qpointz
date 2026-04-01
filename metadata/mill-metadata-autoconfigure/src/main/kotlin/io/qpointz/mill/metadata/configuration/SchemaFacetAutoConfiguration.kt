package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.schema.LogicalLayoutMetadataSource
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacetServiceImpl
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.FacetInstanceReadMerge
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
    MetadataCoreConfiguration::class,
    MetadataEntityServiceAutoConfiguration::class
)
@ConditionalOnClass(SchemaFacetService::class)
class SchemaFacetAutoConfiguration {

    /**
     * Inferred structural / layout facets from the active [SchemaProvider] (SPEC §3g).
     *
     * @param schemaProvider physical schema snapshot source
     * @return read-only metadata contributor merged after repository rows
     */
    @Bean
    @ConditionalOnBean(SchemaProvider::class)
    @ConditionalOnMissingBean(LogicalLayoutMetadataSource::class)
    fun logicalLayoutMetadataSource(schemaProvider: SchemaProvider): LogicalLayoutMetadataSource =
        LogicalLayoutMetadataSource(schemaProvider)

    /**
     * @param schemaProvider physical schema source
     * @param entityRepository `metadata_entity` rows
     * @param facetReadMerge layered facet read merge (SPEC §3i)
     * @param facetCatalog facet type definitions for schema shaping
     * @return schema / metadata merge service
     */
    @Bean
    @ConditionalOnMissingBean(SchemaFacetService::class)
    @ConditionalOnBean(
        SchemaProvider::class,
        MetadataEntityRepository::class,
        FacetInstanceReadMerge::class,
        FacetCatalog::class
    )
    fun schemaFacetService(
        schemaProvider: SchemaProvider,
        entityRepository: MetadataEntityRepository,
        facetReadMerge: FacetInstanceReadMerge,
        facetCatalog: FacetCatalog
    ): SchemaFacetService = SchemaFacetServiceImpl(
        schemaProvider,
        entityRepository,
        facetReadMerge,
        facetCatalog
    )
}
