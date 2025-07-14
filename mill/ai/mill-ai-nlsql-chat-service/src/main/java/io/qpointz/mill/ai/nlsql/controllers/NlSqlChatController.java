package io.qpointz.mill.ai.nlsql.controllers;

import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.model.pojo.ChatMessage;
import io.qpointz.mill.ai.nlsql.services.NlSqlChatService;
import io.qpointz.mill.excepions.statuses.MIllNotFoundStatusException;
import io.qpointz.mill.excepions.statuses.MillStatuses;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/nl2sql", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:8080"})
@ConditionalOnService("ai-nl2data")
public class NlSqlChatController {

    private final NlSqlChatService chatService;

    public NlSqlChatController(NlSqlChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(value = "/chats", consumes = MediaType.ALL_VALUE)
    @ResponseStatus(HttpStatus.OK )
    public List<Chat> listChats() {
        return chatService.listChats();
    }

    @PostMapping(value = "/chats")
    @Operation(summary = "Creates new chat")
    public Chat createChat(@RequestBody Chat.CreateChatRequest request) {
        return chatService.createChat(request);
    }

    @GetMapping(value = "/chats/{chatId}", consumes = MediaType.ALL_VALUE)
    public Chat getChat(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        return chatService.getChat(chatId)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    @PatchMapping(value = "/chats/{chatId}")
    public Chat updateChat(@PathVariable("chatId") UUID chatId, Chat.UpdateChatRequest request) throws MIllNotFoundStatusException {
        return chatService.updateChat(chatId, request)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    @DeleteMapping(value = "/chats/{chatId}")
    public void deleteChat(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        if (!chatService.deleteChat(chatId)) {
            throw MillStatuses.notFound("Chat not found");
        }
    }

    @GetMapping(value = "/chats/{chatId}/messages", consumes = MediaType.ALL_VALUE)
    public List<ChatMessage> listChatMessages(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        return chatService.listChatMessages(chatId)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    @PostMapping(value = "/chats/{chatId}/messages")
    public ChatMessage postChatMessages(@PathVariable("chatId") UUID chatId, @RequestBody Chat.SendChatMessageRequest request) throws MIllNotFoundStatusException {
        return chatService.postChatMessage(chatId, request)
                .orElseThrow(()-> MillStatuses.notFound("Chat not found"));
    }

    @GetMapping (value = "/chats/{chatId}/stream", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> chatStream(@PathVariable("chatId") UUID chatId) throws MIllNotFoundStatusException {
        return chatService.chatStrtream(chatId)
                .orElseThrow(() -> MillStatuses.notFound("Chat not found"));
    }


}
