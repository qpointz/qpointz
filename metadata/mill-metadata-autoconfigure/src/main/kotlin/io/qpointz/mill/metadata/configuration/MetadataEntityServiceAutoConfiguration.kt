package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.api.MetadataEntityController
import io.qpointz.mill.metadata.api.MetadataFacetController
import io.qpointz.mill.metadata.api.MetadataScopeController
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.MetadataScopeService
import io.qpointz.mill.metadata.service.MetadataService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

/**
 * Auto-configures the read-only metadata REST controllers and scope service.
 *
 * Registers [MetadataEntityController] when a [MetadataService] bean is present,
 * [MetadataFacetController] when a [FacetCatalog] bean is present, and
 * [MetadataScopeController] when a [MetadataScopeService] bean is present.
 *
 * The [MetadataScopeService] is registered when a [MetadataScopeRepository] bean is present.
 * Controllers are registered as explicit beans (not via component-scan) so that dependent
 * modules control when they appear.
 */
@AutoConfiguration
class MetadataEntityServiceAutoConfiguration {

    /**
     * Creates the [MetadataEntityController] bean.
     *
     * @param svc the [MetadataService] to delegate entity lookups to
     * @return a configured [MetadataEntityController] instance
     */
    @Bean
    @ConditionalOnBean(MetadataService::class)
    fun metadataEntityController(svc: MetadataService): MetadataEntityController =
        MetadataEntityController(svc)

    /**
     * Creates the [MetadataFacetController] bean.
     *
     * @param catalog the [FacetCatalog] to delegate facet type management to
     * @return a configured [MetadataFacetController] instance
     */
    @Bean
    @ConditionalOnBean(FacetCatalog::class)
    fun metadataFacetController(catalog: FacetCatalog): MetadataFacetController =
        MetadataFacetController(catalog)

    /**
     * Creates the [MetadataScopeService] bean when a [MetadataScopeRepository] is available.
     *
     * @param repo the [MetadataScopeRepository] to delegate to
     * @return a configured [MetadataScopeService] instance
     */
    @Bean
    @ConditionalOnBean(MetadataScopeRepository::class)
    fun metadataScopeService(repo: MetadataScopeRepository): MetadataScopeService =
        MetadataScopeService(repo)

    /**
     * Creates the [MetadataScopeController] bean when a [MetadataScopeService] is available.
     *
     * @param svc the [MetadataScopeService] to delegate scope management to
     * @return a configured [MetadataScopeController] instance
     */
    @Bean
    @ConditionalOnBean(MetadataScopeService::class)
    fun metadataScopeController(svc: MetadataScopeService): MetadataScopeController =
        MetadataScopeController(svc)
}
