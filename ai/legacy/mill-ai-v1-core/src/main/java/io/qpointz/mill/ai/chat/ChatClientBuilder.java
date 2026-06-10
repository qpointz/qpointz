package io.qpointz.mill.ai.chat;

import org.springframework.ai.chat.client.ChatClient;

public interface ChatClientBuilder {

    ChatClient build();

}
