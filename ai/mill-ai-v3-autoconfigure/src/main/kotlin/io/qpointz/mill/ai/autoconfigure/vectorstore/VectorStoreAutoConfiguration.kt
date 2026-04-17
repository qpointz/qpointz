package io.qpointz.mill.ai.autoconfigure.vectorstore

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.config.VectorStoreConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Single [EmbeddingStore] bean per application: in-memory (default) or Chroma HTTP ([ChromaEmbeddingStore]).
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
            VectorStoreConfigurationProperties.Backend.CHROMA ->
                chromaEmbeddingStore(props)
        }
    }

    private fun chromaEmbeddingStore(props: VectorStoreConfigurationProperties): EmbeddingStore<TextSegment> {
        val c = props.chroma
        val baseUrl = c.baseUrl?.trim().orEmpty()
        if (baseUrl.isEmpty()) {
            throw IllegalStateException(
                "mill.ai.vector-store.chroma.base-url is required when mill.ai.vector-store.backend is chroma",
            )
        }
        val apiVersion = when (c.apiVersion) {
            VectorStoreConfigurationProperties.Chroma.ApiVersion.V1 -> ChromaApiVersion.V1
            VectorStoreConfigurationProperties.Chroma.ApiVersion.V2 -> ChromaApiVersion.V2
        }
        log.info(
            "Chroma vector store baseUrl={} apiVersion={} collection={}",
            baseUrl,
            apiVersion,
            c.collectionName,
        )
        return ChromaEmbeddingStore.builder()
            .apiVersion(apiVersion)
            .baseUrl(baseUrl)
            .tenantName(c.tenantName)
            .databaseName(c.databaseName)
            .collectionName(c.collectionName)
            .timeout(c.timeout)
            .build()
    }
}
