package io.qpointz.mill.ai.nlsql.components;

import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import java.io.IOException;

import static io.qpointz.mill.ai.chat.prompts.PromptTemplates.staticTemplate;

/**
 * Factory methods for constructing {@link ChatClient.Builder} instances configured with the
 * prompts, advisors, and memory settings used by the NL2SQL flows.
 */
public class ChatBuilders {

    /**
     * Creates a builder for the primary NL2SQL chat workflow using the default system prompt.
     *
     * @param chatModel model that will back the chat client
     * @return configured builder instance
     * @throws IOException if the system prompt template cannot be loaded
     */
    public static ChatClient.Builder mainChat(ChatModel chatModel) throws IOException {
        return mainChat(ChatClient.builder(chatModel));
    }

    /**
     * Applies the main-system prompt to the provided builder.
     *
     * @param builder existing builder to configure
     * @return builder with a default system message configured
     * @throws IOException if the system prompt template cannot be loaded
     */
    public static ChatClient.Builder mainChat(ChatClient.Builder builder) throws IOException {
        val systemPrompt = staticTemplate(
                "prompts/main-system.prompt",
                ChatBuilders.class.getClassLoader());
        return builder
                .defaultSystem(systemPrompt.render());
    }

    /**
     * Creates a main workflow builder that also maintains chat memory across requests.
     *
     * @param chatModel chat model backing the client
     * @param chatMemory memory implementation used to persist conversation history
     * @return configured builder instance
     * @throws IOException if the system prompt template cannot be loaded
     */
    public static ChatClient.Builder mainChat(ChatModel chatModel, ChatMemory chatMemory) throws IOException {
        return mainChat(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                        //PromptChatMemoryAdvisor.builder(chatMemory).build()
                );
    }

    /**
     * Creates a builder for the reasoning pass that assists with error diagnosis.
     *
     * @param chatModel chat model backing the client
     * @return configured builder instance
     * @throws IOException if the system prompt template cannot be loaded
     */
    public static ChatClient.Builder reasoningChat(ChatModel chatModel) throws IOException {
        return reasoningChat(ChatClient.builder(chatModel));
    }

    /**
     * Creates a reasoning builder with chat memory enabled.
     *
     * @param chatModel chat model backing the client
     * @param memory memory implementation used to persist conversation history
     * @return configured builder instance
     * @throws IOException if the system prompt template cannot be loaded
     */
    public static ChatClient.Builder reasoningChat(ChatModel chatModel, ChatMemory memory) throws IOException {
        return reasoningChat(ChatClient.builder(chatModel))
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build()
                        //PromptChatMemoryAdvisor.builder(memory).build()
                );
    }

    /**
     * Applies the reasoning-system prompt to the provided builder.
     *
     * @param builder existing builder to configure
     * @return builder configured for the reasoning workflow
     * @throws IOException if the system prompt template cannot be loaded
     */
    public static ChatClient.Builder reasoningChat(ChatClient.Builder builder) throws IOException {
        val systemProtmpt = staticTemplate(
                "prompts/reasoning-system.prompt",
                ChatBuilders.class.getClassLoader());
        return builder
                .defaultSystem(systemProtmpt.render());
    }

}
