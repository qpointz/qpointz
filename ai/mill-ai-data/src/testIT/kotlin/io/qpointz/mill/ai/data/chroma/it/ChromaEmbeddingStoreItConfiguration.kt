package io.qpointz.mill.ai.data.chroma.it

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Test-only Chroma [EmbeddingStore] (opt-in IT). Fixed URL/collection for local Chroma; in-memory vector
 * autoconfig is omitted when this bean is used.
 */
@Configuration
open class ChromaEmbeddingStoreItConfiguration {

    @Bean
    open fun chromaEmbeddingStoreForValueMappingIt(): EmbeddingStore<TextSegment> =
        ChromaEmbeddingStore.builder()
            .apiVersion(ChromaApiVersion.V2)
            .baseUrl("http://localhost:8000")
            .tenantName("default_tenant")
            .databaseName("default_database")
            .collectionName("mill-value-mapping-it")
            .timeout(Duration.ofSeconds(60))
            .build()
}
