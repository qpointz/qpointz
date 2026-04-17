package io.qpointz.mill.ai.autoconfigure.valuemap

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.valuemap.ColumnDistinctValueLoader
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingIndexedAttributeDiscovery
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingRefreshConfigurationBridge
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingRefreshOrchestrator
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Wires [ValueMappingRefreshOrchestrator] and startup / scheduled refresh.
 *
 * **Split from [ValueMappingRefreshLifecycleAutoConfiguration]** so prerequisite beans from that configuration
 * (including [ValueMappingIndexedAttributeDiscovery] and [ColumnDistinctValueLoader]) are registered first
 * ([AutoConfigureAfter]).
 *
 * Gated only on [MetadataFacetJpaRepository] (same signal as [ValueMappingRefreshLifecycleAutoConfiguration]'s
 * discovery bean): multi-type [ConditionalOnBean] lists are fragile during auto-config ordering, and sibling
 * `@Bean` [ConditionalOnBean] cannot see beans defined in the same configuration class.
 */
@ConditionalOnAiEnabled
@ConditionalOnBean(MetadataFacetJpaRepository::class)
@AutoConfiguration(
    after = [
        EmbeddingAutoConfiguration::class,
        ValueMappingSyncAutoConfiguration::class,
        ValueMappingRefreshLifecycleAutoConfiguration::class,
    ],
)
@AutoConfigureAfter(
    EmbeddingAutoConfiguration::class,
    ValueMappingSyncAutoConfiguration::class,
    ValueMappingRefreshLifecycleAutoConfiguration::class,
)
class ValueMappingRefreshOrchestratorAutoConfiguration {

    @Bean
    fun valueMappingRefreshOrchestrator(
        refreshConfig: ValueMappingRefreshConfigurationBridge,
        attributeDiscovery: ValueMappingIndexedAttributeDiscovery,
        facetRepository: FacetRepository,
        valueMappingService: ValueMappingService,
        refreshStateRepository: ValueMappingRefreshStateRepository,
        embeddingRepository: ValueMappingEmbeddingRepository,
        embeddingHarness: EmbeddingHarness,
        columnLoader: ColumnDistinctValueLoader,
        schemaProvider: ObjectProvider<SchemaProvider>,
    ): ValueMappingRefreshOrchestrator =
        ValueMappingRefreshOrchestrator(
            refreshConfig,
            attributeDiscovery,
            facetRepository,
            valueMappingService,
            refreshStateRepository,
            embeddingRepository,
            embeddingHarness,
            columnLoader,
            schemaProvider.ifAvailable,
        )

    /**
     * Runs the APP_STARTUP pass once the application context is **fully started** (after web server, if any).
     * Using [ApplicationReadyEvent] instead of [org.springframework.boot.ApplicationRunner] avoids racing
     * metadata or data-plane beans that finish wiring only during the final startup phase.
     *
     * Implemented as an explicit [ApplicationListener] (not a Kotlin lambda) so IDEs can breakpoint
     * [ValueMappingRefreshApplicationReadyListener.onApplicationEvent] reliably.
     */
    @Bean
    @ConditionalOnBean(ValueMappingRefreshOrchestrator::class)
    fun valueMappingRefreshApplicationReadyListener(
        orchestrator: ValueMappingRefreshOrchestrator,
    ): ApplicationListener<ApplicationReadyEvent> =
        ValueMappingRefreshApplicationReadyListener(orchestrator)

    /**
     * @see valueMappingRefreshApplicationReadyListener
     */
    private class ValueMappingRefreshApplicationReadyListener(
        private val orchestrator: ValueMappingRefreshOrchestrator,
    ) : ApplicationListener<ApplicationReadyEvent> {
        override fun onApplicationEvent(event: ApplicationReadyEvent) {
            orchestrator.runStartup()
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(ValueMappingRefreshOrchestrator::class)
    @ConditionalOnProperty(
        value = ["mill.ai.value-mapping.refresh.scheduled-disabled"],
        havingValue = "false",
        matchIfMissing = true,
    )
    @EnableScheduling
    class ValueMappingRefreshSchedulingConfiguration {

        @Bean
        fun valueMappingRefreshScheduler(
            orchestrator: ValueMappingRefreshOrchestrator,
        ): ValueMappingRefreshScheduler = ValueMappingRefreshScheduler(orchestrator)
    }
}
