package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.CallSpecsChatClientBuilders;
import io.qpointz.mill.ai.nlsql.model.UserChat;
import lombok.Getter;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Holds per-chat reactive state: conversation-scoped chat builders and SSE sink.
 * Critical path: sink is replay(5) to ensure late subscribers get recent events; changing replay size
 * impacts UI catch-up behavior.
 */
public class ChatSession {

    @Getter
    private final UserChat chat;

    @Getter
    private final Sinks.Many<ServerSentEvent<?>> sink;

    @Getter
    private final CallSpecsChatClientBuilders chatBuilders;

    /**
     * Constructs a session with dedicated chat memory/builder and replayable SSE sink.
     */
    public ChatSession(UserChat chat, ChatModel model, ChatMemory chatMemory) {
        this.chat = chat;
        this.sink = Sinks.many()
                .replay()
                .limit(5);
        this.chatBuilders = new CallSpecsChatClientBuilders(model, chatMemory, conversationId());
    }

    /**
     * Stable conversation id used for chat memory partitioning.
     */
    public String conversationId() {
        return this.chat.getId().toString();
    }

    /**
     * Returns a flux of SSE events for the chat.
     */
    public Flux<ServerSentEvent<?>> stream() {
        return this.sink
                .asFlux();
    }

    /**
     * Emits an event to connected subscribers.
     */
    public <T> void sendEvent(T entity, String event) {
        this.getSink()
                .tryEmitNext(ServerSentEvent
                        .builder(entity)
                        .event(event)
                        .build());
    }

    public <T> void sendEvent(T entity) {
        this.sendEvent(entity, "chat_message_event");
    }

}
