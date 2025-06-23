package io.qpointz.mill.ai.nlsql.components;

import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;

import java.io.IOException;
import java.util.UUID;

import static io.qpointz.mill.ai.chat.prompts.PromptTemplates.staticTemplate;

public class ChatBuilders {

    public static ChatClient.Builder mainChat(ChatModel chatModel) throws IOException {
        return mainChat(ChatClient.builder(chatModel));
    }

    public static ChatClient.Builder mainChat(ChatClient.Builder builder) throws IOException {
        val systemPrompt = staticTemplate(
                "prompts/main-system.prompt",
                ChatBuilders.class.getClassLoader());
        return builder
                .defaultSystem(systemPrompt.render());
    }

    public static ChatClient.Builder mainChat(ChatModel chatModel, ChatMemory chatMemory) throws IOException {
        return mainChat(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                        //PromptChatMemoryAdvisor.builder(chatMemory).build()
                );
    }

    public static ChatClient.Builder reasoningChat(ChatModel chatModel) throws IOException {
        return reasoningChat(ChatClient.builder(chatModel));
    }

    public static ChatClient.Builder reasoningChat(ChatModel chatModel, ChatMemory memory) throws IOException {
        return reasoningChat(ChatClient.builder(chatModel))
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build()
                        //PromptChatMemoryAdvisor.builder(memory).build()
                );
    }

    public static ChatClient.Builder reasoningChat(ChatClient.Builder builder) throws IOException {
        val systemProtmpt = staticTemplate(
                "prompts/reasoning-system.prompt",
                ChatBuilders.class.getClassLoader());
        return builder
                .defaultSystem(systemProtmpt.render());
    }

}
