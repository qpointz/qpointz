package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.MetadataScopeService
import io.qpointz.mill.metadata.service.MetadataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Auto-configures the metadata service layer: [MetadataService] and [MetadataScopeService].
 *
 * Controllers are registered via component scan ([@RestController][org.springframework.web.bind.annotation.RestController])
 * and resolve their service dependencies at instantiation time, matching the pattern used
 * by other Mill service modules.
 */
@AutoConfiguration
@AutoConfigureAfter(MetadataJpaPersistenceAutoConfiguration::class, MetadataRepositoryAutoConfiguration::class)
class MetadataEntityServiceAutoConfiguration {

    /**
     * Creates the [MetadataService] bean.
     *
     * [MetadataRepository] is always present (JPA, file, or NoOp fallback), so no
     * [org.springframework.boot.autoconfigure.condition.ConditionalOnBean] guard is needed.
     *
     * @param repository   the active [MetadataRepository] implementation
     * @param facetCatalog optional [FacetCatalog]; may be absent in minimal configurations
     * @return a configured [MetadataService] instance
     */
    @Bean
    @ConditionalOnMissingBean(MetadataService::class)
    fun metadataService(
        repository: MetadataRepository,
        @Autowired(required = false) facetCatalog: FacetCatalog?
    ): MetadataService = MetadataService(repository, facetCatalog)

    /**
     * Creates the [MetadataScopeService] bean.
     *
     * [MetadataScopeRepository] is always present (JPA or NoOp fallback).
     *
     * @param repo the [MetadataScopeRepository] to delegate to
     * @return a configured [MetadataScopeService] instance
     */
    @Bean
    @ConditionalOnMissingBean(MetadataScopeService::class)
    fun metadataScopeService(repo: MetadataScopeRepository): MetadataScopeService =
        MetadataScopeService(repo)
}
