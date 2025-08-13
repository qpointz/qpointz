package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.chat.ChatClientBuilder;
import io.qpointz.mill.ai.chat.ChatClientBuilders;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;

public class CallSpecsChatClientBuilders {


    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private final String conversationId;

    public CallSpecsChatClientBuilders(ChatModel model, ChatMemory chatMemory, String conversationId) {
        this.chatModel = model;
        this.chatMemory = chatMemory;
        this.conversationId = conversationId;
    }

    public ChatClientBuilder reasoningChat() {
        return ChatClientBuilders
                .defaultBuilder(ChatClient.builder(this.chatModel)
                    .defaultAdvisors(
                            MessageChatMemoryAdvisor.builder(this.chatMemory)
                                    .conversationId(this.conversationId+"_reason")
                                    .build()
                    ));
    }

    public ChatClientBuilder conversationChat() {
        return ChatClientBuilders.defaultBuilder(
                ChatClient.builder(this.chatModel)
                        .defaultAdvisors(
                            MessageChatMemoryAdvisor.builder(this.chatMemory)
                                .conversationId(this.conversationId)
                                .build()
                        ));

    }

}
