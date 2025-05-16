package io.qpointz.mill.services.llm.configuration;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.llm.PromptBuilder;
import io.qpointz.mill.services.llm.SqlAgent;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Configuration
@ConditionalOnService("data-bot")
@ConfigurationProperties(prefix = "mill.services.data-bot")
@EnableWebSecurity
@Service
public class LLMConfiguration {

    @Bean
    public PromptBuilder promptBuilder(@Value("${mill.services.data-bot.prompt-file}") File promptFile) throws IOException {
        return PromptBuilder.fromFile(promptFile);
    }

    /*@Bean
    public ChatModel chatModel(@Value("${mill.services.data-bot.api-key}") String key,
                               @Value("${mill.services.data-bot.model-name}") String model) {
        return OpenAiChatModel.builder()
                .apiKey(key)
                .modelName(model)
                .temperature(0.2)
                .build();
    }*/

    @Bean
    public SqlAgent sqlAgent(@Autowired @Lazy ChatClient.Builder chatClientBuilder ,
                             @Autowired @Lazy PromptBuilder promptBuilder) {
        val inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();

        val chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(200)
                .chatMemoryRepository(inMemoryChatMemoryRepository)
                .build();

        val chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        PromptChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
        return new SqlAgent(chatClient, promptBuilder);
    }

}
