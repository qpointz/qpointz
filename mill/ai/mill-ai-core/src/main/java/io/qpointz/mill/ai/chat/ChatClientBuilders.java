package io.qpointz.mill.ai.chat;


import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;

public final class ChatClientBuilders {

    private  ChatClientBuilders() {
        //keep empty to avoid construction
    }

    @AllArgsConstructor
    public static class DefaultBuilder implements ChatClientBuilder {

        private final ChatClient.Builder chatClientBuilder;

        @Override
        public ChatClient build() {
            return chatClientBuilder.build();
        }
    }

    public static ChatClientBuilder defaultBuilder(ChatClient.Builder builder) {
        return new DefaultBuilder(builder);
    }

}
