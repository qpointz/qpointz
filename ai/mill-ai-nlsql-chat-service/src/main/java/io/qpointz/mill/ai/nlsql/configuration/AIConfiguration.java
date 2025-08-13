package io.qpointz.mill.ai.nlsql.configuration;


import io.qpointz.mill.services.annotations.ConditionalOnService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@ConditionalOnService("ai-nl2data")
public class AIConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mill.ai.chat", name = "memory", havingValue = "in-memory")
    public ChatMemory millAiChatMemoryinMemory() {
        return createChatMemory(new InMemoryChatMemoryRepository());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mill.ai.chat", name = "memory", havingValue = "jdbc")
    public ChatMemory nlssqlChatMemory(JdbcChatMemoryRepository repository) {
        return createChatMemory(repository);
    }

    private static ChatMemory createChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

}
