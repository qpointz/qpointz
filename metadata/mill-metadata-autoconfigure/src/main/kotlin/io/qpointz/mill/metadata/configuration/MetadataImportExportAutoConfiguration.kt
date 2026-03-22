package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.api.MetadataImportExportController
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataChangeObserver
import io.qpointz.mill.metadata.domain.MetadataChangeObserverChain
import io.qpointz.mill.metadata.domain.MetadataChangeObserverDelegate
import io.qpointz.mill.metadata.domain.NoOpMetadataChangeObserver
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.service.DefaultMetadataImportService
import io.qpointz.mill.metadata.service.MetadataImportService
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource

/**
 * Auto-configures import/export infrastructure: the observer chain, the
 * [MetadataImportService] implementation, the [MetadataImportExportController], and the
 * optional startup import runner.
 *
 * The observer chain is assembled from all [MetadataChangeObserverDelegate] beans present in the
 * context. The chain itself is a [MetadataChangeObserver] but not a [MetadataChangeObserverDelegate],
 * which avoids the circular-injection problem where a `List<MetadataChangeObserver>` would include
 * the chain itself.
 */
@AutoConfiguration
@EnableConfigurationProperties(MetadataProperties::class)
class MetadataImportExportAutoConfiguration {

    /**
     * Assembles all [MetadataChangeObserverDelegate] beans into a single [MetadataChangeObserver].
     *
     * Returns [NoOpMetadataChangeObserver] when no delegates are registered, and
     * [MetadataChangeObserverChain] when one or more delegates are present.
     *
     * @param delegates all [MetadataChangeObserverDelegate] beans in the context
     * @return the composite [MetadataChangeObserver] to inject into services
     */
    @Bean
    @ConditionalOnMissingBean(MetadataChangeObserver::class)
    fun metadataChangeObserverChain(
        delegates: List<MetadataChangeObserverDelegate>
    ): MetadataChangeObserver =
        if (delegates.isEmpty()) NoOpMetadataChangeObserver
        else MetadataChangeObserverChain(delegates)

    /**
     * Creates the [MetadataImportService] implementation when [MetadataRepository] and
     * [FacetTypeRepository] beans are available.
     *
     * @param repo          the entity persistence store
     * @param facetTypeRepo the facet type descriptor store
     * @param observer      the composite change observer for event emission
     * @return a [DefaultMetadataImportService] instance
     */
    @Bean
    @ConditionalOnBean(MetadataRepository::class)
    fun metadataImportService(
        repo: MetadataRepository,
        facetTypeRepo: FacetTypeRepository,
        observer: MetadataChangeObserver
    ): MetadataImportService = DefaultMetadataImportService(repo, facetTypeRepo, observer)

    /**
     * Creates the [MetadataImportExportController] when a [MetadataImportService] bean is
     * available.
     *
     * @param svc the import/export service to delegate to
     * @return a configured [MetadataImportExportController] instance
     */
    @Bean
    @ConditionalOnBean(MetadataImportService::class)
    fun metadataImportExportController(svc: MetadataImportService): MetadataImportExportController =
        MetadataImportExportController(svc)

    /**
     * Registers a startup [ApplicationRunner] that imports metadata from the configured
     * classpath resource immediately after the application context is fully initialised.
     *
     * Only active when `mill.metadata.import-on-startup` is set to a non-empty value.
     * Import runs in [ImportMode.MERGE] mode with `actorId = "system"`.
     *
     * @param svc   the import service to invoke
     * @param props the bound properties containing the resource path
     * @return an [ApplicationRunner] that performs the startup import
     */
    @Bean
    @ConditionalOnProperty(prefix = "mill.metadata", name = ["import-on-startup"])
    fun metadataStartupImportRunner(
        svc: MetadataImportService,
        props: MetadataProperties
    ): ApplicationRunner = ApplicationRunner {
        val path = props.importOnStartup ?: return@ApplicationRunner
        val inputStream = ClassPathResource(path).inputStream
        svc.import(inputStream, ImportMode.MERGE, actorId = "system")
    }
}
