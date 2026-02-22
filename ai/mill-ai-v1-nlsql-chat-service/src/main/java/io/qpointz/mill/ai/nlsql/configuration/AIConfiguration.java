package io.qpointz.mill.ai.nlsql.configuration;


import io.qpointz.mill.ai.nlsql.ValueMapper;
import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.ai.nlsql.components.DefaultValueMapper;
import io.qpointz.mill.ai.nlsql.components.DefaultValueRepository;
import io.qpointz.mill.ai.nlsql.components.VectorStoreValueMapper;
import io.qpointz.mill.data.backend.annotations.ConditionalOnService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Declares AI-related beans: chat memory, vector store, and value mapping.
 */
@Configuration
@Component
@ConditionalOnService("ai-nl2data")
public class AIConfiguration {

    /**
     * AI-related beans: chat memory, vector store, and value mapping.
     */
    /**
     * In-memory chat memory window for lightweight/local profiles.
     */
    @Bean
    @ConditionalOnProperty(prefix = "mill.ai.chat", name = "memory", havingValue = "in-memory")
    public ChatMemory millAiChatMemoryinMemory() {
        return createChatMemory(new InMemoryChatMemoryRepository());
    }

    /**
     * JDBC-backed chat memory window when persistence is configured.
     */
    @Bean
    @ConditionalOnProperty(prefix = "mill.ai.chat", name = "memory", havingValue = "jdbc")
    public ChatMemory nlssqlChatMemory(JdbcChatMemoryRepository repository) {
        return createChatMemory(repository);
    }

    /**
     * Common builder for windowed chat memory; critical for prompt grounding and clarification continuity.
     */
    private static ChatMemory createChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    /**
     * Vector store used for value mapping / retrieval in NL→SQL flows.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore
                .builder(embeddingModel)
                .build();
    }

    /**
     * Repository providing value mappings; defaults to a vector-backed store.
     */
    @Bean
    public ValueRepository defaultValueRepository(VectorStore vectorStore) {
        return new DefaultValueRepository(vectorStore);
    }

    /**
     * Mapper that translates user-provided values using configured repository (vector-backed when available).
     */
    @Bean
    public ValueMapper defaultValueMapper(@Autowired(required = true) ValueRepository defaultValueRepository) {
        return defaultValueRepository != null
                ? new VectorStoreValueMapper(defaultValueRepository)
                : new DefaultValueMapper();
    }

}
