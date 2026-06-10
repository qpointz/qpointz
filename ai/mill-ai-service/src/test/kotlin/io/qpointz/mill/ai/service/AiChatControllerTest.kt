package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ConversationTurn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.Instant

@WebFluxTest(controllers = [AiChatController::class])
@TestPropertySource(properties = [
    "mill.ai.enabled=true",
    "server.error.include-message=always",
    "server.error.include-binding-errors=always",
    "logging.level.org.springframework.web=DEBUG",
    "logging.level.org.springframework.http.codec=DEBUG",
])
class AiChatControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

    @MockitoBean
    private lateinit var chatService: ChatService

    // ── Test fixtures ─────────────────────────────────────────────────────────

    private fun chatMeta(
        chatId: String = "chat-1",
        chatType: String = "general",
        chatName: String = "Test Chat",
    ) = ChatMetadata(
        chatId = chatId,
        userId = "user-1",
        profileId = "hello-world",
        chatName = chatName,
        chatType = chatType,
        isFavorite = false,
        contextType = null,
        contextId = null,
        contextLabel = null,
        contextEntityType = null,
        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2025-01-01T00:00:00Z"),
    )

    private fun chatView(meta: ChatMetadata = chatMeta(), turns: List<ConversationTurn> = emptyList()) =
        ChatView(chat = meta, messages = turns)

    // ── GET /api/v1/ai/chats ──────────────────────────────────────────────────

    @Test
    fun `listChats should return 200 with chat list`() {
        whenever(chatService.listChats()).thenReturn(listOf(chatMeta()))

        client.get().uri("/api/v1/ai/chats")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].chatId").isEqualTo("chat-1")
            .jsonPath("$[0].chatName").isEqualTo("Test Chat")
    }

    // ── POST /api/v1/ai/chats ─────────────────────────────────────────────────

    @Test
    fun `createChat should return 201 for new general chat`() {
        val meta = chatMeta()
        whenever(chatService.createChat(any())).thenReturn(ChatCreationResult(meta, created = true))

        client.post().uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.chatId").isEqualTo("chat-1")
    }

    @Test
    fun `createChat should return 200 when contextual chat already exists`() {
        val meta = chatMeta(chatType = "contextual")
        whenever(chatService.createChat(any())).thenReturn(ChatCreationResult(meta, created = false))

        client.post().uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"contextType":"model","contextId":"sales.customers"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chatId").isEqualTo("chat-1")
    }

    // ── GET /api/v1/ai/chats/{chatId} ─────────────────────────────────────────

    @Test
    fun `getChat should return 200 with chat detail`() {
        val turn = ConversationTurn(
            turnId = "t-1",
            role = "user",
            text = "Hello",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )
        whenever(chatService.getChat("chat-1")).thenReturn(chatView(turns = listOf(turn)))

        client.get().uri("/api/v1/ai/chats/chat-1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chat.chatId").isEqualTo("chat-1")
            .jsonPath("$.messages[0].turnId").isEqualTo("t-1")
    }

    @Test
    fun `getChat should return 404 when chat not found`() {
        whenever(chatService.getChat("missing")).thenReturn(null)

        client.get().uri("/api/v1/ai/chats/missing")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    // ── PATCH /api/v1/ai/chats/{chatId} ──────────────────────────────────────

    @Test
    fun `updateChat should return 200 with updated metadata`() {
        val updated = chatMeta(chatName = "Renamed")
        whenever(chatService.updateChat(eq("chat-1"), any())).thenReturn(updated)

        client.patch().uri("/api/v1/ai/chats/chat-1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"chatName":"Renamed"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chatName").isEqualTo("Renamed")
    }

    @Test
    fun `updateChat should return 404 when chat not found`() {
        whenever(chatService.updateChat(eq("missing"), any())).thenReturn(null)

        client.patch().uri("/api/v1/ai/chats/missing")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"chatName":"X"}""")
            .exchange()
            .expectStatus().isNotFound
    }

    // ── DELETE /api/v1/ai/chats/{chatId} ─────────────────────────────────────

    @Test
    fun `deleteChat should return 204 on success`() {
        whenever(chatService.deleteChat("chat-1")).thenReturn(true)

        client.delete().uri("/api/v1/ai/chats/chat-1")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `deleteChat should return 404 when chat not found`() {
        whenever(chatService.deleteChat("missing")).thenReturn(false)

        client.delete().uri("/api/v1/ai/chats/missing")
            .exchange()
            .expectStatus().isNotFound
    }

    // ── GET /api/v1/ai/chats/{chatId}/messages ────────────────────────────────

    @Test
    fun `listMessages should return 200 with turn list`() {
        val turn = ConversationTurn(
            turnId = "t-2",
            role = "assistant",
            text = "Hi there",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )
        whenever(chatService.getChat("chat-1")).thenReturn(chatView(turns = listOf(turn)))

        client.get().uri("/api/v1/ai/chats/chat-1/messages")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].turnId").isEqualTo("t-2")
            .jsonPath("$[0].role").isEqualTo("assistant")
    }

    @Test
    fun `listMessages should return 404 when chat not found`() {
        whenever(chatService.getChat("missing")).thenReturn(null)

        client.get().uri("/api/v1/ai/chats/missing/messages")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    // ── POST /api/v1/ai/chats/{chatId}/messages ───────────────────────────────

    @Test
    fun `sendMessage should return 200 SSE stream`() {
        whenever(chatService.getChat("chat-1")).thenReturn(chatView())
        whenever(chatService.sendMessage(eq("chat-1"), eq("Hello"))).thenReturn(
            Flux.just(
                ChatRuntimeEvent.Chunk("Hi"),
                ChatRuntimeEvent.Completed("Hi"),
            )
        )

        val response = client.post().uri("/api/v1/ai/chats/chat-1/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"Hello"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)

        val events = response.responseBody.collectList().block()!!
        // Streaming path: ItemCreated, ItemPartUpdated, ItemCompleted
        assertThat(events).isNotEmpty
    }

    @Test
    fun `sendMessage should return 404 when chat not found`() {
        // Pre-flight check returns null → 404 before SSE stream is opened
        whenever(chatService.getChat("missing")).thenReturn(null)

        client.post().uri("/api/v1/ai/chats/missing/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"Hello"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `sendMessage should deliver runtime failures as in-stream item_failed event`() {
        // Chat exists (pre-flight passes) but the model/runtime fails after stream opens
        whenever(chatService.getChat("chat-1")).thenReturn(chatView())
        whenever(chatService.sendMessage(eq("chat-1"), any())).thenReturn(
            Flux.error(RuntimeException("model unavailable"))
        )

        val response = client.post().uri("/api/v1/ai/chats/chat-1/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"Hello"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk  // HTTP 200 — error is inside the stream
            .returnResult(String::class.java)

        val events = response.responseBody.collectList().block()!!
        assertThat(events.any { it.contains("item.failed") }).isTrue()
    }

    // ── GET /api/v1/ai/chats/context-types/{contextType}/contexts/{contextId} ─

    @Test
    fun `getChatByContext should return 200 when chat exists`() {
        val meta = chatMeta(chatType = "contextual")
        whenever(chatService.getChatByContext("model", "sales.customers")).thenReturn(meta)

        client.get().uri("/api/v1/ai/chats/context-types/model/contexts/sales.customers")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chatId").isEqualTo("chat-1")
    }

    @Test
    fun `getChatByContext should return 404 when no chat for context`() {
        whenever(chatService.getChatByContext("model", "unknown")).thenReturn(null)

        client.get().uri("/api/v1/ai/chats/context-types/model/contexts/unknown")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}
