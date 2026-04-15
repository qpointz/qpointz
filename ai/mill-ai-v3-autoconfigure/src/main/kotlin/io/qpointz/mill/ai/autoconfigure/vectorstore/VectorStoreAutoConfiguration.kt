package io.qpointz.mill.ai.autoconfigure.vectorstore

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.config.VectorStoreConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Single [EmbeddingStore] bean per application (MVP: in-memory).
 */
@ConditionalOnAiEnabled
@AutoConfiguration
@EnableConfigurationProperties(VectorStoreConfigurationProperties::class)
class VectorStoreAutoConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnMissingBean(EmbeddingStore::class)
    fun embeddingStore(props: VectorStoreConfigurationProperties): EmbeddingStore<TextSegment> {
        log.info("Vector store backend={}", props.backend)
        return when (props.backend) {
            VectorStoreConfigurationProperties.Backend.IN_MEMORY ->
                InMemoryEmbeddingStore()
        }
    }
}
