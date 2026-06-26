package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.autoconfigure.chat.LangChain4jChatRuntime
import io.qpointz.mill.ai.persistence.ArtifactObserver
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.FacetArtifactEventPublisher
import io.qpointz.mill.ai.service.ArtifactLifecycleService
import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.service.facet.FacetArtifactScopeService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import io.qpointz.mill.events.configuration.EventsAutoConfiguration

/**
 * Wires facet artefact lifecycle: event publish on capture, scope assign/retract consumers,
 * and Accept/Reject service (WI-360).
 */
@ConditionalOnAiEnabled
@AutoConfiguration(
    after = [
        AiV3AutoConfiguration::class,
        EventsAutoConfiguration::class,
    ],
)
class AiV3ArtifactLifecycleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(
        EventPublisher::class,
        FacetRepository::class,
        FacetTypeDefinitionRepository::class,
        FacetTypeRepository::class,
        EntityRepository::class,
    )
    fun facetArtifactScopeService(
        facetRepository: FacetRepository,
        facetTypeDefinitionRepository: FacetTypeDefinitionRepository,
        facetTypeRepository: FacetTypeRepository,
        entityRepository: EntityRepository,
    ): FacetArtifactScopeService = FacetArtifactScopeService(
        facetRepository = facetRepository,
        facetTypeDefinitionRepository = facetTypeDefinitionRepository,
        facetTypeRepository = facetTypeRepository,
        entityRepository = entityRepository,
    )

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EventPublisher::class)
    fun facetArtifactEventPublisher(eventPublisher: EventPublisher): FacetArtifactEventPublisher =
        FacetArtifactEventPublisher(eventPublisher)

    @Bean
    @ConditionalOnMissingBean(name = ["facetArtifactObserver"])
    @ConditionalOnBean(FacetArtifactEventPublisher::class)
    fun facetArtifactObserver(publisher: FacetArtifactEventPublisher): ArtifactObserver = publisher

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EventPublisher::class, ArtifactStore::class)
    fun artifactLifecycleService(
        artifactStore: ArtifactStore,
        eventPublisher: EventPublisher,
    ): ArtifactLifecycleService = ArtifactLifecycleService(
        artifactStore = artifactStore,
        eventPublisher = eventPublisher,
    )
}
