package io.qpointz.mill.ai.nlsql.services;

import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.model.pojo.ChatMessage;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service contract for NL→SQL chat lifecycle and messaging operations.
 */
public interface NlSqlChatService {

    /**
     * Lists chats for the current user.
     */
    List<Chat> listChats();
    /**
     * Creates a chat seeded with the initial user message.
     */
    Chat createChat(Chat.CreateChatRequest request);
    /**
     * Retrieves chat metadata by id.
     */
    Optional<Chat> getChat(UUID chatId);
    /**
     * Updates chat properties (name, favorite).
     */
    Optional<Chat> updateChat(UUID chatId, Chat.UpdateChatRequest request);
    /**
     * Deletes a chat and returns true when removed.
     */
    boolean deleteChat(UUID chatId);

    /**
     * Lists messages for a chat.
     */
    Optional<List<ChatMessage>> listChatMessages(UUID chatId);
    /**
     * Posts a user message and triggers downstream processing.
     */
    Optional<ChatMessage> postChatMessage(UUID chatId, Chat.SendChatMessageRequest request);
    /**
     * Opens an SSE stream for chat events.
     */
    Optional<Flux<ServerSentEvent<?>>> chatStrtream(UUID chatId);
}
