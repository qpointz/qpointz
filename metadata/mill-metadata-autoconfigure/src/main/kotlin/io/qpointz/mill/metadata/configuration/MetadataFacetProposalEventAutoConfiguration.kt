package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.events.api.EventConsumer
import io.qpointz.mill.events.configuration.EventsAutoConfiguration
import io.qpointz.mill.metadata.events.FacetProposalEventConsumers
import io.qpointz.mill.metadata.service.facet.FacetArtifactScopeService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Wires metadata consumers for facet proposal lifecycle events (scope assign / retract).
 */
@AutoConfiguration
@AutoConfigureAfter(EventsAutoConfiguration::class)
@ConditionalOnClass(EventConsumer::class)
@ConditionalOnBean(FacetArtifactScopeService::class)
class MetadataFacetProposalEventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun facetProposalEventConsumers(
        scopeService: FacetArtifactScopeService,
    ): FacetProposalEventConsumers = FacetProposalEventConsumers(scopeService)

    @Bean
    @ConditionalOnMissingBean(name = ["facetProposalEventConsumer"])
    @ConditionalOnBean(FacetProposalEventConsumers::class)
    fun facetProposalEventConsumer(consumers: FacetProposalEventConsumers): EventConsumer =
        consumers.consumer()
}
