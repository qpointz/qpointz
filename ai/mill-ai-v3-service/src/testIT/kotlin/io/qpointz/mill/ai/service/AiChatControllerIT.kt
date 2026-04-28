package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.ConversationTurn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Full-cycle integration tests for [AiChatController].
 *
 * Runs against a real Spring Boot context with:
 * - H2 in-memory JPA (via mill-ai-v3-persistence + spring-boot-starter-data-jpa)
 * - A stub [AiV3ChatRuntime] that echoes the message and persists turns without OpenAI
 * - The full WebFlux stack on a random port
 *
 * Covers: create, list, get, rename, favourite, delete, send message (SSE),
 * message transcript, title derivation, context lookup, and 404 paths.
 */
@SpringBootTest(
    classes = [AiChatServiceITApplication::class, AiChatControllerIT.StubRuntimeConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("testIT")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AiChatControllerIT {

    // ── Stub runtime ──────────────────────────────────────────────────────────

    /**
     * Replaces [io.qpointz.mill.ai.autoconfigure.chat.LangChain4jChatRuntime] from autoconfigure.
     * Echoes the user message, persists both turns to [ConversationStore], and
     * emits the three standard SSE event types without calling OpenAI.
     */
    @TestConfiguration
    class StubRuntimeConfig {
        @Autowired
        private lateinit var conversationStore: ConversationStore

        @Bean
        fun stubAiV3ChatRuntime(): AiV3ChatRuntime = AiV3ChatRuntime { metadata, message ->
            Flux.defer {
                val reply = "Echo: $message"
                conversationStore.appendTurn(
                    metadata.chatId,
                    ConversationTurn(
                        turnId = UUID.randomUUID().toString(),
                        role = "user",
                        text = message,
                        createdAt = Instant.now(),
                    ),
                )
                conversationStore.appendTurn(
                    metadata.chatId,
                    ConversationTurn(
                        turnId = UUID.randomUUID().toString(),
                        role = "assistant",
                        text = reply,
                        createdAt = Instant.now(),
                    ),
                )
                Flux.just<ChatRuntimeEvent>(
                    ChatRuntimeEvent.Chunk("Echo: "),
                    ChatRuntimeEvent.Chunk(message),
                    ChatRuntimeEvent.Completed(reply),
                )
            }.subscribeOn(Schedulers.boundedElastic())
        }
    }

    // ── Test client ───────────────────────────────────────────────────────────

    @LocalServerPort
    private var port: Int = 0

    private lateinit var client: WebTestClient
    private lateinit var sseClient: WebTestClient

    @BeforeEach
    fun setup() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .responseTimeout(Duration.ofSeconds(15))
            .build()
        sseClient = client.mutate()
            .responseTimeout(Duration.ofSeconds(30))
            .build()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun createChat(body: String = "{}"): String =
        client.post().uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectBody()
            .jsonPath("$.chatId").isNotEmpty
            .returnResult()
            .let {
                val json = String(it.responseBody!!)
                // extract chatId from JSON string
                Regex(""""chatId"\s*:\s*"([^"]+)"""").find(json)!!.groupValues[1]
            }

    // ── Create chat ───────────────────────────────────────────────────────────

    @Test
    fun `should return 201 when creating a general chat`() {
        client.post().uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.chatId").isNotEmpty
            .jsonPath("$.chatName").isEqualTo("New Chat")
            .jsonPath("$.chatType").isEqualTo("general")
            .jsonPath("$.isFavorite").isEqualTo(false)
            .jsonPath("$.userId").isEqualTo("it-test-user")
    }

    @Test
    fun `should return 201 when creating a contextual chat`() {
        client.post().uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"contextType":"model","contextId":"sales.customers","contextLabel":"Customers"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.chatId").isNotEmpty
            .jsonPath("$.chatType").isEqualTo("contextual")
            .jsonPath("$.contextType").isEqualTo("model")
            .jsonPath("$.contextId").isEqualTo("sales.customers")
            .jsonPath("$.contextLabel").isEqualTo("Customers")
    }

    @Test
    fun `should return 200 and reuse existing contextual chat on duplicate create`() {
        val firstId = createChat("""{"contextType":"model","contextId":"sales.orders","contextLabel":"Orders"}""")

        client.post().uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"contextType":"model","contextId":"sales.orders","contextLabel":"Orders"}""")
            .exchange()
            .expectStatus().isOk   // 200 — reuse, not 201
            .expectBody()
            .jsonPath("$.chatId").isEqualTo(firstId)
    }

    // ── List chats ────────────────────────────────────────────────────────────

    @Test
    fun `should list only general chats`() {
        createChat()
        createChat()
        createChat("""{"contextType":"model","contextId":"list.test.ctx","contextLabel":"CTX"}""")

        val body = client.get().uri("/api/v1/ai/chats")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Map::class.java)
            .returnResult()
            .responseBody!!

        assertThat(body).allMatch { it["chatType"] == "general" }
        assertThat(body).noneMatch { it["chatType"] == "contextual" }
    }

    // ── Get chat ──────────────────────────────────────────────────────────────

    @Test
    fun `should return 200 with chat detail and empty messages for new chat`() {
        val chatId = createChat()

        client.get().uri("/api/v1/ai/chats/$chatId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chat.chatId").isEqualTo(chatId)
            .jsonPath("$.chat.chatType").isEqualTo("general")
            .jsonPath("$.messages").isArray
            .jsonPath("$.messages.length()").isEqualTo(0)
    }

    @Test
    fun `should return 404 when chat does not exist`() {
        client.get().uri("/api/v1/ai/chats/nonexistent-id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    // ── Rename chat ───────────────────────────────────────────────────────────

    @Test
    fun `should rename chat and return 200 with new name`() {
        val chatId = createChat()

        client.patch().uri("/api/v1/ai/chats/$chatId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"chatName":"My Renamed Chat"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chatId").isEqualTo(chatId)
            .jsonPath("$.chatName").isEqualTo("My Renamed Chat")

        // Verify persisted
        client.get().uri("/api/v1/ai/chats/$chatId")
            .exchange()
            .expectBody()
            .jsonPath("$.chat.chatName").isEqualTo("My Renamed Chat")
    }

    @Test
    fun `should return 404 when renaming a nonexistent chat`() {
        client.patch().uri("/api/v1/ai/chats/does-not-exist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"chatName":"Whatever"}""")
            .exchange()
            .expectStatus().isNotFound
    }

    // ── Favourite ─────────────────────────────────────────────────────────────

    @Test
    fun `should mark chat as favourite and return 200`() {
        val chatId = createChat()

        client.patch().uri("/api/v1/ai/chats/$chatId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"isFavorite":true}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isFavorite").isEqualTo(true)

        // Verify persisted
        client.get().uri("/api/v1/ai/chats/$chatId")
            .exchange()
            .expectBody()
            .jsonPath("$.chat.isFavorite").isEqualTo(true)
    }

    @Test
    fun `should unmark favourite and return 200`() {
        val chatId = createChat()

        // Mark
        client.patch().uri("/api/v1/ai/chats/$chatId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"isFavorite":true}""")
            .exchange()
            .expectStatus().isOk

        // Unmark
        client.patch().uri("/api/v1/ai/chats/$chatId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"isFavorite":false}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isFavorite").isEqualTo(false)
    }

    // ── Delete chat ───────────────────────────────────────────────────────────

    @Test
    fun `should return 204 when deleting an existing chat`() {
        val chatId = createChat()

        client.delete().uri("/api/v1/ai/chats/$chatId")
            .exchange()
            .expectStatus().isNoContent

        // Verify gone
        client.get().uri("/api/v1/ai/chats/$chatId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 when deleting a nonexistent chat`() {
        client.delete().uri("/api/v1/ai/chats/ghost-chat-id")
            .exchange()
            .expectStatus().isNotFound
    }

    // ── Send message / SSE ────────────────────────────────────────────────────

    @Test
    fun `should stream SSE events when sending a message`() {
        val chatId = createChat()

        val lines = sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"Hello integration test"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)
            .responseBody
            .collectList()
            .block(Duration.ofSeconds(30))!!
            .joinToString(" ")

        // Stub: Chunk → ItemCreated + ItemPartUpdated; Chunk → ItemPartUpdated; Completed → ItemCompleted
        assertThat(lines).contains("item.created")
        assertThat(lines).contains("item.part.updated")
        assertThat(lines).contains("item.completed")
    }

    @Test
    fun `should return 404 before opening SSE stream when chat does not exist`() {
        client.post().uri("/api/v1/ai/chats/nonexistent-chat/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"Hello"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isNotFound
    }

    // ── Message transcript ────────────────────────────────────────────────────

    @Test
    fun `should persist user and assistant turns in transcript after sending message`() {
        val chatId = createChat()

        // Consume SSE stream fully — turns are persisted during streaming
        sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"What is 2+2?"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .returnResult(String::class.java)
            .responseBody
            .collectList()
            .block(Duration.ofSeconds(30))

        client.get().uri("/api/v1/ai/chats/$chatId/messages")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].role").isEqualTo("user")
            .jsonPath("$[0].text").isEqualTo("What is 2+2?")
            .jsonPath("$[1].role").isEqualTo("assistant")
            .jsonPath("$[1].text").isEqualTo("Echo: What is 2+2?")
    }

    @Test
    fun `should return 404 when listing messages for nonexistent chat`() {
        client.get().uri("/api/v1/ai/chats/no-such-chat/messages")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    // ── Title derivation ──────────────────────────────────────────────────────

    @Test
    fun `should derive chat title from first message and truncate at 30 chars`() {
        val chatId = createChat()
        val longMessage = "This is a very long question that exceeds thirty characters"

        sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"$longMessage"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .returnResult(String::class.java)
            .responseBody
            .collectList()
            .block(Duration.ofSeconds(30))

        client.get().uri("/api/v1/ai/chats/$chatId")
            .exchange()
            .expectBody()
            .jsonPath("$.chat.chatName").isEqualTo("This is a very long question t...")
    }

    @Test
    fun `should keep short first-message as the chat title without truncation`() {
        val chatId = createChat()

        sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"Short message"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .returnResult(String::class.java)
            .responseBody
            .collectList()
            .block(Duration.ofSeconds(30))

        client.get().uri("/api/v1/ai/chats/$chatId")
            .exchange()
            .expectBody()
            .jsonPath("$.chat.chatName").isEqualTo("Short message")
    }

    // ── Context lookup ────────────────────────────────────────────────────────

    @Test
    fun `should return chat by context type and context id`() {
        val chatId = createChat("""{"contextType":"knowledge","contextId":"concept.clv","contextLabel":"CLV"}""")

        client.get().uri("/api/v1/ai/chats/context-types/knowledge/contexts/concept.clv")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chatId").isEqualTo(chatId)
            .jsonPath("$.contextType").isEqualTo("knowledge")
            .jsonPath("$.contextId").isEqualTo("concept.clv")
    }

    @Test
    fun `should return 404 when no chat exists for the given context`() {
        client.get().uri("/api/v1/ai/chats/context-types/model/contexts/nonexistent.entity")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    // ── Full lifecycle scenario ───────────────────────────────────────────────

    @Test
    fun `full lifecycle - create, rename, favourite, send, read transcript, delete`() {
        // 1. Create
        val chatId = createChat()
        client.get().uri("/api/v1/ai/chats/$chatId").exchange()
            .expectBody().jsonPath("$.chat.chatName").isEqualTo("New Chat")

        // 2. Rename
        client.patch().uri("/api/v1/ai/chats/$chatId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"chatName":"Integration Test Chat"}""")
            .exchange().expectStatus().isOk

        // 3. Mark favourite
        client.patch().uri("/api/v1/ai/chats/$chatId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"isFavorite":true}""")
            .exchange().expectStatus().isOk
            .expectBody().jsonPath("$.isFavorite").isEqualTo(true)

        // 4. Send message — consume SSE stream
        sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"Hello lifecycle"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .returnResult(String::class.java)
            .responseBody
            .collectList()
            .block(Duration.ofSeconds(30))

        // 5. Verify transcript
        client.get().uri("/api/v1/ai/chats/$chatId/messages")
            .exchange().expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].role").isEqualTo("user")
            .jsonPath("$[1].role").isEqualTo("assistant")

        // 6. Verify persisted state after rename + favourite + message-title-derivation
        client.get().uri("/api/v1/ai/chats/$chatId").exchange()
            .expectBody()
            .jsonPath("$.chat.chatName").isEqualTo("Integration Test Chat")
            .jsonPath("$.chat.isFavorite").isEqualTo(true)

        // 7. Delete
        client.delete().uri("/api/v1/ai/chats/$chatId")
            .exchange().expectStatus().isNoContent

        // 8. Confirm gone
        client.get().uri("/api/v1/ai/chats/$chatId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().isNotFound
    }

    // ── Profiles (GET /api/v1/ai/profiles) ────────────────────────────────────

    @Test
    fun `should list registered profiles`() {
        client.get().uri("/api/v1/ai/profiles")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].id").isEqualTo("hello-world")
            .jsonPath("$[1].id").isEqualTo("schema-authoring")
            .jsonPath("$[2].id").isEqualTo("schema-exploration")
    }

    @Test
    fun `should get profile by id`() {
        client.get().uri("/api/v1/ai/profiles/hello-world")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("hello-world")
            .jsonPath("$.capabilityIds").isArray
    }

    @Test
    fun `should return 404 for unknown profile id`() {
        client.get().uri("/api/v1/ai/profiles/unknown-profile-id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `schema exploration profile includes metadata without metadata-authoring`() {
        val body = client.get().uri("/api/v1/ai/profiles/schema-exploration")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody!!
        assertThat(body).contains("\"metadata\"")
        assertThat(body).doesNotContain("\"metadata-authoring\"")
    }

    @Test
    fun `schema authoring profile includes metadata and metadata-authoring`() {
        val body = client.get().uri("/api/v1/ai/profiles/schema-authoring")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody!!
        assertThat(body).contains("\"metadata\"")
        assertThat(body).contains("\"metadata-authoring\"")
    }

    @Test
    fun `SSE completes for schema-exploration chat`() {
        val chatId = createChat("{\"profileId\":\"schema-exploration\"}")
        sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"message\":\"hi\"}")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `SSE completes for schema-authoring chat`() {
        val chatId = createChat("{\"profileId\":\"schema-authoring\"}")
        sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"message\":\"hi\"}")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
    }

}