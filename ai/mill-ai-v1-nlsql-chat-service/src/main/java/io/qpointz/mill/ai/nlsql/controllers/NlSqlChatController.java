package io.qpointz.mill.ai.nlsql.controllers;

import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.model.pojo.ChatMessage;
import io.qpointz.mill.ai.nlsql.services.NlSqlChatService;
import io.qpointz.mill.excepions.statuses.MIllNotFoundStatusException;
import io.qpointz.mill.excepions.statuses.MillStatuses;
import io.qpointz.mill.data.backend.annotations.ConditionalOnService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

/**
 * REST API surface for NL→SQL chat: chat lifecycle, messaging, and SSE streaming.
 */
@RestController
@RequestMapping(value = "/api/nl2sql", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:8080"})
@ConditionalOnService("ai-nl2data")
@lombok.extern.slf4j.Slf4j
public class NlSqlChatController {

    private final NlSqlChatService chatService;

    public NlSqlChatController(NlSqlChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Lists chats for the current user (or anonymous).
     */
    @GetMapping(value = "/chats", consumes = MediaType.ALL_VALUE)
    @ResponseStatus(HttpStatus.OK )
    public List<Chat> listChats() {
        log.debug("Listing chats");
        return chatService.listChats();
    }

    /**
     * Creates a chat and seeds it with the provided initial user message.
     */
    @PostMapping(value = "/chats")
    @Operation(summary = "Creates new chat")
    public Chat createChat(@RequestBody Chat.CreateChatRequest request) {
        log.info("Creating chat with name='{}'", request.name());
        return chatService.createChat(request);
    }

    /**
     * Retrieves chat metadata by id.
     */
    @GetMapping(value = "/chats/{chatId}", consumes = MediaType.ALL_VALUE)
    public Chat getChat(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        log.debug("Fetching chat {}", chatId);
        return chatService.getChat(chatId)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    /**
     * Updates chat name or favorite flag.
     */
    @PatchMapping(value = "/chats/{chatId}")
    @Operation(summary = "Updates chat")
    public Chat updateChat(@RequestBody Chat.UpdateChatRequest request, @PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        log.info("Updating chat {}", chatId);
        return chatService.updateChat(chatId, request)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    /**
     * Deletes a chat and its messages.
     */
    @DeleteMapping(value = "/chats/{chatId}", consumes = MediaType.ALL_VALUE, produces = MediaType.ALL_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes chat chat")
    public void deleteChat(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        log.info("Deleting chat {}", chatId);
        if (!chatService.deleteChat(chatId)) {
            throw MillStatuses.notFound("Chat not found");
        }
    }

    /**
     * Lists messages for the given chat.
     */
    @GetMapping(value = "/chats/{chatId}/messages", consumes = MediaType.ALL_VALUE)
    public List<ChatMessage> listChatMessages(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        log.debug("Listing messages for chat {}", chatId);
        return chatService.listChatMessages(chatId)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    /**
     * Posts a user message and triggers async processing.
     */
    @PostMapping(value = "/chats/{chatId}/messages")
    public ChatMessage postChatMessages(@PathVariable("chatId") UUID chatId, @RequestBody Chat.SendChatMessageRequest request) throws MIllNotFoundStatusException {
        log.info("Posting message to chat {}", chatId);
        return chatService.postChatMessage(chatId, request)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    /**
     * Opens an SSE stream to deliver chat events.
     */
    @GetMapping (value = "/chats/{chatId}/stream", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> chatStream(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        log.debug("Opening chat stream for {}", chatId);
        return chatService.chatStrtream(chatId)
                .orElseThrow(() -> MillStatuses.notFound("Chat not found"));
    }


}
