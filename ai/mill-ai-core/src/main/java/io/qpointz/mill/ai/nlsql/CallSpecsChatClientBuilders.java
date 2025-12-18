package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatClientBuilder;
import io.qpointz.mill.ai.chat.ChatClientBuilders;
import io.qpointz.mill.ai.nlsql.tools.ValueMappingTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ChatModelCallAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;

@Slf4j
public class CallSpecsChatClientBuilders {


    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private final String conversationId;
    private final ValueMapper valueMapper;

    public CallSpecsChatClientBuilders(ChatModel model, ChatMemory chatMemory, String conversationId, ValueMapper valueMapper) {
        this.chatModel = model;
        this.chatMemory = chatMemory;
        this.conversationId = conversationId;
        this.valueMapper = valueMapper;
    }

    public ChatClientBuilder reasoningChat() {
//        return ChatClientBuilders
//                .defaultBuilder(ChatClient.builder(this.chatModel)
//                    .defaultAdvisors(
//                            MessageChatMemoryAdvisor.builder(this.chatMemory)
//                                    .conversationId(this.conversationId+"_reason")
//                                    .build()
//                    ));
        return conversationChat();
    }

    public ChatClientBuilder conversationChat() {

        var modelCallAdvisor = ChatModelCallAdvisor.builder()
                .chatModel(chatModel)
                .build();

        var builder = ChatClient.builder(this.chatModel)
                .defaultAdvisors(
                    modelCallAdvisor,
                    MessageChatMemoryAdvisor.builder(this.chatMemory)
                        .conversationId(this.conversationId)
                        .build()
                );
        
        // Register value mapping tool if ValueMapper is available
        if (this.valueMapper != null) {
            log.info("Use value mapping tool");

//            if (toolCallback != null) {
//                try {
//                    // Use reflection to call defaultToolCallbacks if available
//                    builder.getClass().getMethod("defaultToolCallbacks", Object.class).invoke(builder, toolCallback);
//                } catch (Exception e) {
//                    // Tool registration not available in this Spring AI version
//                    // Fall back to placeholder-based approach
//                }
//            }
            builder.defaultToolCallbacks(ValueMappingTool.createToolCallback(valueMapper));
        }
        
        return ChatClientBuilders.defaultBuilder(builder);
    }

}
