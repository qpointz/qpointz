package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.api.MetadataEntityController
import io.qpointz.mill.metadata.api.MetadataFacetController
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.MetadataService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

/**
 * Auto-configures the read-only metadata REST controllers.
 *
 * Registers [MetadataEntityController] when a [MetadataService] bean is present, and
 * [MetadataFacetController] when a [FacetCatalog] bean is present. Controllers are registered
 * as explicit beans (not via component-scan) so that dependent modules control when they appear.
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
}
