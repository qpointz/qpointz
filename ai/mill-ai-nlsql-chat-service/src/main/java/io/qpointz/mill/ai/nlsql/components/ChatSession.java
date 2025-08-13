package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.CallSpecsChatClientBuilders;
import io.qpointz.mill.ai.nlsql.model.UserChat;
import lombok.Getter;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class ChatSession {

    @Getter
    private final UserChat chat;

    @Getter
    private final Sinks.Many<ServerSentEvent<?>> sink;

    @Getter
    private final CallSpecsChatClientBuilders chatBuilders;

    public ChatSession(UserChat chat, ChatModel model, ChatMemory chatMemory) {
        this.chat = chat;
        this.sink = Sinks.many()
                .replay()
                .limit(5);
        this.chatBuilders = new CallSpecsChatClientBuilders(model, chatMemory, conversationId());
    }

    public String conversationId() {
        return this.chat.getId().toString();
    }

    public Flux<ServerSentEvent<?>> stream() {
        return this.sink
                .asFlux();
    }

    public <T> void sendEvent(T entity) {
        this.getSink()
                .tryEmitNext(ServerSentEvent.builder(entity).build());
    }

}
