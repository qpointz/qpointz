package io.qpointz.mill.ai.autoconfigure.schema

import io.qpointz.mill.ai.autoconfigure.AiV3DataAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.events.api.EventConsumer
import io.qpointz.mill.events.dsl.eventConsumer
import io.qpointz.mill.events.model.ProcessingMode
import io.qpointz.mill.metadata.events.MetadataEventTypes
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Invalidates the AI schema catalog cache when facet proposals are persisted or retracted so
 * subsequent `list_*` tools see updated descriptions and relations within a chat session.
 */
@ConditionalOnAiEnabled
@AutoConfiguration(after = [AiV3DataAutoConfiguration::class])
@AutoConfigureAfter(name = [
    "io.qpointz.mill.metadata.configuration.MetadataFacetProposalEventAutoConfiguration",
])
@ConditionalOnClass(EventConsumer::class, SchemaCatalogPort::class)
@ConditionalOnBean(SchemaCatalogPort::class)
class SchemaCatalogCacheInvalidationAutoConfiguration {

    /**
     * @param catalogPort AI schema catalog (typically a caching [io.qpointz.mill.ai.data.schema.SchemaFacetCatalogAdapter])
     * @return event consumer that clears the catalog cache on facet lifecycle events
     */
    @Bean
    @ConditionalOnMissingBean(name = ["schemaCatalogCacheInvalidationConsumer"])
    fun schemaCatalogCacheInvalidationConsumer(catalogPort: SchemaCatalogPort): EventConsumer =
        eventConsumer {
            on(MetadataEventTypes.FACET_PROPOSAL_PERSISTED, ProcessingMode.SYNC) {
                catalogPort.invalidateCache()
            }
            on(MetadataEventTypes.FACET_PROPOSAL_RETRACTED, ProcessingMode.SYNC) {
                catalogPort.invalidateCache()
            }
        }
}
