package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.domain.MetadataChangeObserver
import io.qpointz.mill.metadata.domain.MetadataChangeObserverChain
import io.qpointz.mill.metadata.domain.MetadataChangeObserverDelegate
import io.qpointz.mill.metadata.domain.NoOpMetadataChangeObserver
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.metadata.service.DefaultMetadataImportService
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.MetadataEntityService
import io.qpointz.mill.metadata.service.MetadataImportService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Auto-configures import/export infrastructure: the observer chain, the
 * [MetadataImportService] implementation, and the [MetadataImportExportController].
 */
@AutoConfiguration
@AutoConfigureAfter(MetadataJpaPersistenceAutoConfiguration::class, MetadataRepositoryAutoConfiguration::class)
@EnableConfigurationProperties(MetadataProperties::class)
class MetadataImportExportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MetadataChangeObserver::class)
    fun metadataChangeObserverChain(
        delegates: List<MetadataChangeObserverDelegate>
    ): MetadataChangeObserver =
        if (delegates.isEmpty()) NoOpMetadataChangeObserver
        else MetadataChangeObserverChain(delegates)

    /**
     * @param entityRepository entity rows for replace / existence
     * @param entityService transactional entity CRUD
     * @param facetRepository facet assignment persistence
     * @param scopeRepository declared scopes from YAML
     * @param facetCatalog facet type registration from YAML definitions
     */
    @Bean
    @ConditionalOnMissingBean(MetadataImportService::class)
    fun metadataImportService(
        entityRepository: EntityRepository,
        entityService: MetadataEntityService,
        facetRepository: FacetRepository,
        scopeRepository: MetadataScopeRepository,
        facetCatalog: FacetCatalog
    ): MetadataImportService = DefaultMetadataImportService(
        entityRepository,
        entityService,
        facetRepository,
        scopeRepository,
        facetCatalog
    )
}
