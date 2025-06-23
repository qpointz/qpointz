package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.model.UserChat;
import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@AllArgsConstructor
public class ChatSessionManager {

    private final ChatModel chatModel;

    private final ChatMemory chatMemory;

    private final ConcurrentHashMap<UUID,ChatSession> sessions =
            new ConcurrentHashMap<>();

    public ChatSession getOrCreate(UserChat of) {
        return this.sessions.computeIfAbsent(of.getId(),
                k -> new ChatSession(of, chatModel, chatMemory));
    }

}
