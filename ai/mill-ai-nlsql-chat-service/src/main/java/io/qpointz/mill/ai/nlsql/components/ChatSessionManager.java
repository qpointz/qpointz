package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueMapper;
import io.qpointz.mill.ai.nlsql.model.UserChat;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Manages per-chat sessions, ensuring each chat has isolated builders and sinks.
 */
@AllArgsConstructor
public class ChatSessionManager {

    private final ChatModel chatModel;

    private final ChatMemory chatMemory;

    private final ValueMapper valueMapper;

    private final ConcurrentHashMap<UUID,ChatSession> sessions =
            new ConcurrentHashMap<>();

    /**
     * Returns an existing chat session or creates one bound to the provided chat entity.
     * Critical path: uses a concurrent map to ensure per-chat session/memory isolation;
     * avoid bypassing computeIfAbsent to prevent duplicate sessions and memory divergence.
     *
     * @param of user chat entity
     * @return session wrapper with chat builders and SSE sink
     */
    public ChatSession getOrCreate(UserChat of) {
        return this.sessions.computeIfAbsent(of.getId(),
                k -> new ChatSession(of, chatModel, chatMemory, valueMapper));
    }

}
