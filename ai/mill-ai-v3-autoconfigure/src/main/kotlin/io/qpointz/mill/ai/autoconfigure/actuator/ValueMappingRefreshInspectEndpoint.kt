package io.qpointz.mill.ai.autoconfigure.actuator

import dev.langchain4j.store.embedding.EmbeddingStore
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.valuemap.ColumnDistinctValueLoader
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.ai.valuemap.VectorMappingSynchronizer
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingIndexedAttributeDiscovery
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingRefreshOrchestrator
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.context.ApplicationContext

/**
 * Read-only Actuator surface for diagnosing value-mapping refresh wiring: which prerequisite beans
 * exist and key `mill.ai.*` properties (no secrets).
 */
@Endpoint(id = "valuemap")
class ValueMappingRefreshInspectEndpoint(
    private val context: ApplicationContext,
) {

    /**
     * Returns a JSON-friendly map of bean presence and selected configuration keys.
     */
    @ReadOperation
    fun snapshot(): Map<String, Any?> {
        val env = context.environment
        return linkedMapOf(
            "mill.ai.enabled" to env.getProperty("mill.ai.enabled"),
            "mill.metadata.repository.type" to env.getProperty("mill.metadata.repository.type"),
            "mill.ai.value-mapping.refresh.startup-enabled" to env.getProperty("mill.ai.value-mapping.refresh.startup-enabled"),
            "mill.ai.value-mapping.refresh.scheduled-disabled" to env.getProperty("mill.ai.value-mapping.refresh.scheduled-disabled"),
            "beans" to mapOf(
                "MetadataFacetJpaRepository" to report(MetadataFacetJpaRepository::class.java),
                "FacetRepository" to report(FacetRepository::class.java),
                "ValueMappingIndexedAttributeDiscovery" to report(ValueMappingIndexedAttributeDiscovery::class.java),
                "ValueMappingEmbeddingRepository" to report(ValueMappingEmbeddingRepository::class.java),
                "VectorMappingSynchronizer" to report(VectorMappingSynchronizer::class.java),
                "ValueMappingService" to report(ValueMappingService::class.java),
                "ValueMappingRefreshStateRepository" to report(ValueMappingRefreshStateRepository::class.java),
                "EmbeddingHarness" to report(EmbeddingHarness::class.java),
                "EmbeddingStore" to report(EmbeddingStore::class.java),
                "ColumnDistinctValueLoader" to report(ColumnDistinctValueLoader::class.java),
                "DataOperationDispatcher" to report(DataOperationDispatcher::class.java),
                "ValueMappingRefreshOrchestrator" to report(ValueMappingRefreshOrchestrator::class.java),
            ),
        )
    }

    private fun report(type: Class<*>): Map<String, Any?> {
        val names = context.getBeanNamesForType(type)
        return mapOf(
            "present" to names.isNotEmpty(),
            "beanNames" to names.toList(),
        )
    }
}
