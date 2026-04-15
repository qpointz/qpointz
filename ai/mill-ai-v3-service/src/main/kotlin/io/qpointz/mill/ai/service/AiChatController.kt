package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.service.dto.ChatDetailResponse
import io.qpointz.mill.ai.service.dto.ChatResponse
import io.qpointz.mill.ai.service.dto.CreateChatHttpRequest
import io.qpointz.mill.ai.service.dto.SendMessageHttpRequest
import io.qpointz.mill.ai.service.dto.TurnResponse
import io.qpointz.mill.ai.service.dto.UpdateChatHttpRequest
import io.qpointz.mill.ai.sse.ChatSseEvent
import io.qpointz.mill.excepions.statuses.MillStatusDetails
import io.qpointz.mill.excepions.statuses.MillStatuses
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

/**
 * HTTP/SSE transport layer for the unified AI v3 chat API.
 *
 * This controller is intentionally thin: it translates HTTP requests/responses and
 * delegates all business logic to [UnifiedChatService]. No orchestration here.
 *
 * Registered when AI v3 is enabled via `mill.ai.enabled` (same gate as
 * [io.qpointz.mill.ai.autoconfigure.AiV3ChatServiceAutoConfiguration]).
 */
@Tag(name = "ai-chat", description = "Unified AI v3 chat lifecycle and messaging")
@RestController
@RequestMapping(
    value = ["/api/v1/ai/chats"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:8080"])
@ConditionalOnAiEnabled
class AiChatController(private val chatService: ChatService) {

    // ── Chat lifecycle ─────────────────────────────────────────────────────────

    @Operation(summary = "List chats for the current user")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Chats returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = ChatResponse::class)))],
        ),
    ])
    @GetMapping(consumes = [MediaType.ALL_VALUE])
    fun listChats(): List<ChatResponse> =
        chatService.listChats().map(ChatResponse::from)

    @Operation(
        summary = "Create or retrieve a chat",
        description = "Creates a general chat when no context fields are set. " +
            "For contextual chats the existing chat for (contextType, contextId) is returned if one already exists. " +
            "Returns 201 when a new chat was created, 200 when an existing contextual singleton was reused.",
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "201",
            description = "New chat created",
            content = [Content(schema = Schema(implementation = ChatResponse::class))],
        ),
        ApiResponse(
            responseCode = "200",
            description = "Existing contextual chat returned (idempotent reuse)",
            content = [Content(schema = Schema(implementation = ChatResponse::class))],
        ),
    ])
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createChat(@RequestBody(required = false) request: CreateChatHttpRequest?): ResponseEntity<ChatResponse> {
        val result = chatService.createChat(request?.toServiceRequest())
        val body = ChatResponse.from(result.chat)
        return if (result.created) ResponseEntity.status(HttpStatus.CREATED).body(body)
               else ResponseEntity.ok(body)
    }

    @Operation(summary = "Get chat with message history")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Chat found",
            content = [Content(schema = Schema(implementation = ChatDetailResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Chat not found",
            content = [Content(schema = Schema(implementation = MillStatusDetails::class))],
        ),
    ])
    @GetMapping(value = ["/{chatId}"], consumes = [MediaType.ALL_VALUE])
    fun getChat(@PathVariable chatId: String): ChatDetailResponse =
        chatService.getChat(chatId)
            ?.let(ChatDetailResponse::from)
            ?: throw MillStatuses.notFound("Chat not found: $chatId")

    @Operation(summary = "Update chat metadata (name, favourite flag)")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Chat updated",
            content = [Content(schema = Schema(implementation = ChatResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Chat not found",
            content = [Content(schema = Schema(implementation = MillStatusDetails::class))],
        ),
    ])
    @PatchMapping(value = ["/{chatId}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateChat(
        @PathVariable chatId: String,
        @RequestBody request: UpdateChatHttpRequest,
    ): ChatResponse =
        chatService.updateChat(chatId, request.toChatUpdate())
            ?.let(ChatResponse::from)
            ?: throw MillStatuses.notFound("Chat not found: $chatId")

    @Operation(summary = "Delete a chat")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Chat deleted"),
        ApiResponse(
            responseCode = "404",
            description = "Chat not found",
            content = [Content(schema = Schema(implementation = MillStatusDetails::class))],
        ),
    ])
    @DeleteMapping(value = ["/{chatId}"], consumes = [MediaType.ALL_VALUE], produces = [MediaType.ALL_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteChat(@PathVariable chatId: String) {
        if (!chatService.deleteChat(chatId)) {
            throw MillStatuses.notFound("Chat not found: $chatId")
        }
    }

    // ── Messages ───────────────────────────────────────────────────────────────

    @Operation(summary = "List durable message transcript for a chat")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Messages returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = TurnResponse::class)))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Chat not found",
            content = [Content(schema = Schema(implementation = MillStatusDetails::class))],
        ),
    ])
    @GetMapping(value = ["/{chatId}/messages"], consumes = [MediaType.ALL_VALUE])
    fun listMessages(@PathVariable chatId: String): List<TurnResponse> =
        chatService.getChat(chatId)
            ?.messages?.map(TurnResponse::from)
            ?: throw MillStatuses.notFound("Chat not found: $chatId")

    @Operation(
        summary = "Send a message and stream the response via SSE",
        description = "Returns a stream of server-sent events. " +
            "Each event `data` field carries a JSON-serialised ChatSseEvent. " +
            "Event types: `item.created`, `item.diagnostic`, `item.part.updated`, `item.tool.call`, " +
            "`item.tool.result`, `item.completed`, `item.failed`. " +
            "`item.diagnostic` carries UX status (code, message, optional detail) before the reply completes. " +
            "Streaming consumers should accumulate `item.part.updated` deltas; " +
            "`item.completed.content` is `null` when deltas were emitted — use the accumulated text. " +
            "`item.completed.content` is non-null only when no deltas preceded (non-streaming fallback path). " +
            "Runtime errors that occur after the stream is opened (e.g. model failures) are delivered " +
            "in-stream as `item.failed` events rather than as HTTP error responses.",
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "SSE stream opened; events follow. " +
                "Runtime failures after stream start are delivered as in-stream `item.failed` events.",
            content = [Content(
                mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                schema = Schema(implementation = ChatSseEvent::class),
            )],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Chat not found — returned before the SSE stream is opened",
            content = [Content(schema = Schema(implementation = MillStatusDetails::class))],
        ),
    ])
    @PostMapping(
        value = ["/{chatId}/messages"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun sendMessage(
        @PathVariable chatId: String,
        @RequestBody request: SendMessageHttpRequest,
    ): Flux<ServerSentEvent<ChatSseEvent>> {
        // Pre-flight: verify the chat exists before opening the SSE stream so callers
        // receive a proper 404 at the HTTP layer rather than an in-stream item.failed.
        chatService.getChat(chatId) ?: throw MillStatuses.notFound("Chat not found: $chatId")

        val mapper = ChatRuntimeEventToSseMapper(chatId)
        return chatService.sendMessage(chatId, request.message)
            .flatMap { event -> Flux.fromIterable(mapper.map(event)) }
            .map { sseEvent ->
                ServerSentEvent.builder(sseEvent)
                    .id(sseEvent.eventId)
                    .event(sseEvent.type)
                    .build()
            }
            .onErrorResume { ex ->
                // Covers runtime failures after the stream is opened (model errors, timeouts, etc.).
                val failed = mapper.fail("agent.error", ex.message ?: "Unknown error")
                Flux.just(
                    ServerSentEvent.builder(failed as ChatSseEvent)
                        .id(failed.eventId)
                        .event(failed.type)
                        .build()
                )
            }
    }

    // ── Context lookup ─────────────────────────────────────────────────────────

    @Operation(summary = "Look up a context-bound chat by (contextType, contextId)")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Context-bound chat found",
            content = [Content(schema = Schema(implementation = ChatResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "No chat exists for this context",
            content = [Content(schema = Schema(implementation = MillStatusDetails::class))],
        ),
    ])
    @GetMapping(
        value = ["/context-types/{contextType}/contexts/{contextId}"],
        consumes = [MediaType.ALL_VALUE],
    )
    fun getChatByContext(
        @PathVariable contextType: String,
        @PathVariable contextId: String,
    ): ChatResponse =
        chatService.getChatByContext(contextType, contextId)
            ?.let(ChatResponse::from)
            ?: throw MillStatuses.notFound("No chat for context $contextType/$contextId")
}
