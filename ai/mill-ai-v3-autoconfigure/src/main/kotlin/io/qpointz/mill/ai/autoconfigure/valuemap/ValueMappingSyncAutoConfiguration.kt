package io.qpointz.mill.ai.autoconfigure.valuemap

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import io.qpointz.mill.ai.autoconfigure.AiV3AutoConfiguration
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.config.ValueMappingConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.vectorstore.VectorStoreAutoConfiguration
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.valuemap.DefaultValueMappingService
import io.qpointz.mill.ai.valuemap.DefaultVectorMappingSynchronizer
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.ai.valuemap.VectorMappingSynchronizer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Wires [VectorMappingSynchronizer] and [ValueMappingService] when repository + harness + store exist.
 */
@ConditionalOnAiEnabled
@AutoConfiguration(
    after = [
        EmbeddingAutoConfiguration::class,
        VectorStoreAutoConfiguration::class,
        AiV3AutoConfiguration::class,
    ],
)
@AutoConfigureAfter(
    EmbeddingAutoConfiguration::class,
    VectorStoreAutoConfiguration::class,
    AiV3AutoConfiguration::class,
)
class ValueMappingSyncAutoConfiguration {

    @Bean
    @ConditionalOnBean(ValueMappingEmbeddingRepository::class, EmbeddingHarness::class, EmbeddingStore::class)
    @ConditionalOnMissingBean(VectorMappingSynchronizer::class)
    fun vectorMappingSynchronizer(
        repository: ValueMappingEmbeddingRepository,
        harness: EmbeddingHarness,
        embeddingStore: EmbeddingStore<TextSegment>,
    ): VectorMappingSynchronizer =
        DefaultVectorMappingSynchronizer(repository, harness, embeddingStore)

    @Bean
    @ConditionalOnBean(VectorMappingSynchronizer::class)
    @ConditionalOnMissingBean(ValueMappingService::class)
    fun valueMappingService(
        synchronizer: VectorMappingSynchronizer,
        vm: ValueMappingConfigurationProperties,
    ): ValueMappingService =
        DefaultValueMappingService(synchronizer, vm.maxContentLength)
}
