package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.chat.UserIdResolver
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.ConversationTurn
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

/**
 * Regression coverage for chat persistence (WI-317–319): JPA-backed registry,
 * per-turn profile history, multi-user ownership, and durable rows before first message.
 */
@SpringBootTest(
    classes = [AiChatServiceITApplication::class, AiChatPersistenceIT.TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("testIT")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AiChatPersistenceIT {

    @TestConfiguration
    class TestConfig {
        @Autowired
        private lateinit var conversationStore: ConversationStore

        @Bean
        fun switchableUserIdResolver(): SwitchableUserIdResolver = SwitchableUserIdResolver()

        @Bean
        @Primary
        fun userIdResolver(switchable: SwitchableUserIdResolver): UserIdResolver = switchable

        @Bean
        fun stubAiV3ChatRuntime(): AiV3ChatRuntime = AiV3ChatRuntime { metadata, message, _ ->
            Flux.defer {
                conversationStore.appendTurn(
                    metadata.chatId,
                    ConversationTurn(
                        turnId = UUID.randomUUID().toString(),
                        role = "user",
                        text = message,
                        profileId = metadata.profileId,
                        createdAt = Instant.now(),
                    ),
                )
                val reply = "Echo: $message"
                conversationStore.appendTurn(
                    metadata.chatId,
                    ConversationTurn(
                        turnId = UUID.randomUUID().toString(),
                        role = "assistant",
                        text = reply,
                        profileId = metadata.profileId,
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

    class SwitchableUserIdResolver : UserIdResolver {
        private val current = AtomicReference("user-a")

        /** Switches the resolved user id for subsequent HTTP requests in the same test. */
        fun asUser(userId: String) {
            current.set(userId)
        }

        override fun resolve(): String = current.get()
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var switchableUserId: SwitchableUserIdResolver

    @Autowired
    private lateinit var conversationStore: ConversationStore

    @Autowired
    private lateinit var chatRegistry: ChatRegistry

    @Autowired
    private lateinit var entityManager: EntityManager

    private lateinit var client: WebTestClient
    private lateinit var sseClient: WebTestClient

    @BeforeEach
    fun setup() {
        switchableUserId.asUser("user-a")
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .responseTimeout(Duration.ofSeconds(15))
            .build()
        sseClient = client.mutate()
            .responseTimeout(Duration.ofSeconds(30))
            .build()
    }

    @Test
    fun `should persist ai_chat row on create before first message`() {
        val chatId = createChat()

        assertThat(chatRegistry.load(chatId)).isNotNull
        assertThat(conversationStore.load(chatId)?.turns.orEmpty()).isEmpty()
    }

    @Test
    @Transactional
    fun `should reload chat and transcript from JPA after entity manager clear`() {
        val chatId = createChat()
        sendMessage(chatId, "persist me")

        entityManager.flush()
        entityManager.clear()

        assertThat(chatRegistry.load(chatId)).isNotNull
        val record = conversationStore.load(chatId)
        assertThat(record?.turns).hasSize(2)
        assertThat(record!!.turns[0].text).isEqualTo("persist me")
    }

    @Test
    fun `should record per-turn profileId after mid-chat profile switch`() {
        val chatId = createChat("""{"profileId":"hello-world"}""")
        sendMessage(chatId, "first exchange")

        client.patch().uri("/api/v1/ai/chats/$chatId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"profileId":"schema-exploration"}""")
            .exchange()
            .expectStatus().isOk

        sendMessage(chatId, "second exchange")

        client.get().uri("/api/v1/ai/chats/$chatId/messages")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(4)
            .jsonPath("$[0].profileId").isEqualTo("hello-world")
            .jsonPath("$[1].profileId").isEqualTo("hello-world")
            .jsonPath("$[2].profileId").isEqualTo("schema-exploration")
            .jsonPath("$[3].profileId").isEqualTo("schema-exploration")

        client.get().uri("/api/v1/ai/chats/$chatId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.chat.profileId").isEqualTo("schema-exploration")
    }

    @Test
    fun `should isolate chats by user and hide cross-user access`() {
        switchableUserId.asUser("user-a")
        val chatA = createChat()

        switchableUserId.asUser("user-b")
        createChat()

        client.get().uri("/api/v1/ai/chats")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].userId").isEqualTo("user-b")

        client.get().uri("/api/v1/ai/chats/$chatA")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound

        client.patch().uri("/api/v1/ai/chats/$chatA")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"chatName":"stolen"}""")
            .exchange()
            .expectStatus().isNotFound

        client.delete().uri("/api/v1/ai/chats/$chatA")
            .exchange()
            .expectStatus().isNotFound

        sseClient.post().uri("/api/v1/ai/chats/$chatA/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"nope"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isNotFound
    }

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
                Regex(""""chatId"\s*:\s*"([^"]+)"""").find(json)!!.groupValues[1]
            }

    private fun sendMessage(chatId: String, message: String) {
        sseClient.post().uri("/api/v1/ai/chats/$chatId/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"message":"$message"}""")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .returnResult(String::class.java)
            .responseBody
            .collectList()
            .block(Duration.ofSeconds(30))
    }
}
