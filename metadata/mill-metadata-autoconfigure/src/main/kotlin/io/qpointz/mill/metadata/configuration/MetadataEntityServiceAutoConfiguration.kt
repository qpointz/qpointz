package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.data.schema.DefaultMetadataEntityUrnCodec
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.source.MetadataSource
import io.qpointz.mill.metadata.source.RepositoryMetadataSource
import io.qpointz.mill.metadata.repository.MetadataAuditRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.service.FacetInstanceReadMerge
import io.qpointz.mill.metadata.service.DefaultFacetService
import io.qpointz.mill.metadata.service.DefaultMetadataEditService
import io.qpointz.mill.metadata.service.DefaultMetadataEntityService
import io.qpointz.mill.metadata.service.DefaultMetadataService
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.FacetService
import io.qpointz.mill.metadata.service.MetadataEditService
import io.qpointz.mill.metadata.service.MetadataEntityService
import io.qpointz.mill.metadata.service.MetadataReader
import io.qpointz.mill.metadata.service.MetadataScopeService
import io.qpointz.mill.metadata.service.MetadataService
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Auto-configures the metadata service layer: entity CRUD, facet assignment, read facade, and scope service.
 */
@AutoConfiguration
@AutoConfigureAfter(MetadataJpaPersistenceAutoConfiguration::class, MetadataRepositoryAutoConfiguration::class)
class MetadataEntityServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MetadataEntityUrnCodec::class)
    fun metadataEntityUrnCodec(): MetadataEntityUrnCodec = DefaultMetadataEntityUrnCodec()

    @Bean
    @ConditionalOnMissingBean(MetadataEntityService::class)
    fun metadataEntityService(
        entityRepository: MetadataEntityRepository,
        facetRepository: FacetRepository
    ): MetadataEntityService = DefaultMetadataEntityService(entityRepository, facetRepository)

    @Bean
    @ConditionalOnMissingBean(MetadataReader::class)
    fun metadataReader(facetCatalog: FacetCatalog): MetadataReader = MetadataReader(facetCatalog)

    /**
     * Read-only repository-backed [io.qpointz.mill.metadata.source.MetadataSource] for layered facet resolution.
     *
     * @param facetRepository persisted facet assignments
     * @param metadataReader scope merge for effective rows
     */
    @Bean
    @ConditionalOnMissingBean(RepositoryMetadataSource::class)
    fun repositoryMetadataSource(
        facetRepository: FacetRepository,
        metadataReader: MetadataReader
    ): RepositoryMetadataSource = RepositoryMetadataSource(facetRepository, metadataReader)

    /**
     * Merges facet rows from all [MetadataSource] contributors for [io.qpointz.mill.metadata.service.FacetService.resolve].
     *
     * @param metadataSources all beans implementing [MetadataSource] (order stabilized by [FacetInstanceReadMerge] via origin id)
     * @param facetCatalog facet type cardinality lookup
     */
    @Bean
    @ConditionalOnMissingBean(FacetInstanceReadMerge::class)
    fun facetInstanceReadMerge(
        metadataSources: List<MetadataSource>,
        facetCatalog: FacetCatalog
    ): FacetInstanceReadMerge = FacetInstanceReadMerge(metadataSources, facetCatalog)

    @Bean
    @ConditionalOnMissingBean(FacetService::class)
    fun facetService(
        facetRepository: FacetRepository,
        facetCatalog: FacetCatalog,
        facetTypeRepository: FacetTypeRepository,
        readMerge: FacetInstanceReadMerge
    ): FacetService = DefaultFacetService(facetRepository, facetCatalog, facetTypeRepository, readMerge)

    @Bean
    @ConditionalOnMissingBean(MetadataService::class)
    fun metadataService(
        entityService: MetadataEntityService,
        urnCodec: MetadataEntityUrnCodec
    ): MetadataService = DefaultMetadataService(entityService, urnCodec)

    @Bean
    @ConditionalOnMissingBean(MetadataEditService::class)
    fun metadataEditService(
        entityService: MetadataEntityService,
        facetService: FacetService,
        facetRepository: FacetRepository,
        auditRepository: MetadataAuditRepository,
        urnCodec: MetadataEntityUrnCodec
    ): MetadataEditService = DefaultMetadataEditService(
        entityService,
        facetService,
        facetRepository,
        auditRepository,
        urnCodec
    )

    @Bean
    @ConditionalOnMissingBean(MetadataScopeService::class)
    fun metadataScopeService(repo: MetadataScopeRepository): MetadataScopeService =
        MetadataScopeService(repo)
}
