package io.qpointz.mill.ai.nlsql.services;

import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.model.pojo.ChatMessage;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NlSqlChatService {

    List<Chat> listChats();
    Chat createChat(Chat.CreateChatRequest request);
    Optional<Chat> getChat(UUID chatId);
    Optional<Chat> updateChat(UUID chatId, Chat.UpdateChatRequest request);
    boolean deleteChat(UUID chatId);

    Optional<List<ChatMessage>> listChatMessages(UUID chatId);
    Optional<ChatMessage> postChatMessage(UUID chatId, Chat.SendChatMessageRequest request);
    Optional<Flux<ServerSentEvent<?>>> chatStrtream(UUID chatId);
}
