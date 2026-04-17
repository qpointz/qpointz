package io.qpointz.mill.ai.autoconfigure.valuemap

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.ai.valuemap.VectorMappingSynchronizer
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingIndexedAttributeDiscovery
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingRefreshOrchestrator
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.AiEmbeddingModelRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingStateJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Emits a single WARN when AI is enabled but [ValueMappingRefreshOrchestrator] was not registered,
 * listing missing prerequisite beans so operators can fix configuration without reading condition code.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class ValueMappingRefreshPrerequisiteLogger : ApplicationListener<ContextRefreshedEvent> {

    private val log = LoggerFactory.getLogger(javaClass)
    private var logged: Boolean = false

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (event.applicationContext.parent != null) {
            return
        }
        if (logged) {
            return
        }
        val ctx = event.applicationContext
        if (ctx.getBeanNamesForType(ValueMappingRefreshOrchestrator::class.java).isNotEmpty()) {
            return
        }
        // Only care when AI autoconfigure is active (embedding / chat beans present).
        if (ctx.getBeanNamesForType(EmbeddingHarness::class.java).isEmpty()) {
            return
        }

        logged = true
        val missing = buildList {
            if (ctx.getBeanNamesForType(MetadataFacetJpaRepository::class.java).isEmpty()) {
                add(
                    "MetadataFacetJpaRepository (set mill.metadata.repository.type=jpa and ensure metadata JPA autoconfigure runs)",
                )
            }
            if (ctx.getBeanNamesForType(ValueMappingIndexedAttributeDiscovery::class.java).isEmpty()) {
                add("ValueMappingIndexedAttributeDiscovery (requires MetadataFacetJpaRepository)")
            }
            if (ctx.getBeanNamesForType(FacetRepository::class.java).isEmpty()) {
                add("FacetRepository")
            }
            if (ctx.getBeanNamesForType(AiEmbeddingModelRepository::class.java).isEmpty()) {
                add(
                    "AiEmbeddingModelRepository (Flyway V6+ ai tables; JPA repo scan io.qpointz.mill.persistence.ai.jpa.repositories)",
                )
            }
            if (ctx.getBeanNamesForType(ValueMappingEmbeddingRepository::class.java).isEmpty()) {
                add(
                    "ValueMappingEmbeddingRepository (needs AiEmbeddingModelRepository + AiValueMappingRepository beans from AiV3JpaConfiguration)",
                )
            }
            if (ctx.getBeanNamesForType(EmbeddingStore::class.java).isEmpty()) {
                add("EmbeddingStore (VectorStoreAutoConfiguration in-memory default)")
            }
            if (ctx.getBeanNamesForType(VectorMappingSynchronizer::class.java).isEmpty()) {
                add(
                    "VectorMappingSynchronizer (needs ValueMappingEmbeddingRepository + EmbeddingHarness + EmbeddingStore)",
                )
            }
            if (ctx.getBeanNamesForType(ValueMappingService::class.java).isEmpty()) {
                add("ValueMappingService (needs VectorMappingSynchronizer)")
            }
            if (ctx.getBeanNamesForType(AiValueMappingStateJpaRepository::class.java).isEmpty()) {
                add(
                    "AiValueMappingStateJpaRepository (Flyway V7+ ai_value_mapping_state; see mill-persistence migrations)",
                )
            }
            if (ctx.getBeanNamesForType(ValueMappingRefreshStateRepository::class.java).isEmpty()) {
                add(
                    "ValueMappingRefreshStateRepository (AiV3ValueMappingStateAutoConfiguration when AiValueMappingStateJpaRepository exists)",
                )
            }
        }
        if (missing.isEmpty()) {
            log.warn(
                "ValueMappingRefreshOrchestrator is missing but prerequisite beans appear present — " +
                    "check ColumnDistinctValueLoader or an unexpected bean override.",
            )
        } else {
            log.warn(
                "ValueMappingRefreshOrchestrator was not registered. Missing or inactive: {}",
                missing.joinToString("; "),
            )
        }
    }
}
