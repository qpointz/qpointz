package io.qpointz.mill.ai.data.chroma.it

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.valuemap.DefaultValueMappingService
import io.qpointz.mill.ai.valuemap.DefaultVectorMappingSynchronizer
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.ai.valuemap.VectorMappingSynchronizer
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaValueMappingEmbeddingAdapter
import io.qpointz.mill.persistence.ai.jpa.repositories.AiEmbeddingModelRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Explicit beans for the Skymill Chroma IT slice.
 *
 * Wires [JpaValueMappingEmbeddingAdapter] here so the context does not rely on
 * [io.qpointz.mill.ai.autoconfigure.AiV3JpaConfiguration]'s `ConditionalOnBean(AiEmbeddingModelRepository)` matching
 * before Spring Data repository beans finish registration (ordering-sensitive in sliced imports).
 */
@Configuration
open class ChromaSkymillValueMappingBeansConfiguration {

    @Bean
    open fun chromaItValueMappingEmbeddingRepository(
        modelRepo: AiEmbeddingModelRepository,
        valueRepo: AiValueMappingRepository,
    ): ValueMappingEmbeddingRepository = JpaValueMappingEmbeddingAdapter(modelRepo, valueRepo)

    @Bean
    open fun chromaItVectorMappingSynchronizer(
        repository: ValueMappingEmbeddingRepository,
        harness: EmbeddingHarness,
        embeddingStore: EmbeddingStore<TextSegment>,
    ): VectorMappingSynchronizer = DefaultVectorMappingSynchronizer(repository, harness, embeddingStore)

    @Bean
    open fun chromaItValueMappingService(synchronizer: VectorMappingSynchronizer): ValueMappingService =
        DefaultValueMappingService(synchronizer)
}
