package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.ai.persistence.RunEventRecord
import io.qpointz.mill.ai.persistence.RunEventStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for [AiScenarioExportController] when scenario capture is enabled.
 */
@SpringBootTest(
    classes = [AiChatServiceITApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("testIT")
@TestPropertySource(properties = ["mill.ai.chat.scenario-capture.enabled=true"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AiScenarioExportControllerIT {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var conversationStore: ConversationStore

    @Autowired
    private lateinit var runEventStore: RunEventStore

    private lateinit var client: WebTestClient

    @BeforeEach
    fun setUp() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun shouldExportYaml_whenChatHasUserTurnsAndCapturedEvents() {
        val chatId = UUID.randomUUID().toString()
        val createResponse = client.post()
            .uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"profileId":"hello-world"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .returnResult()
            .responseBody
            ?.let { String(it) }
            ?: error("missing create response")

        val createdChatId = Regex(""""chatId"\s*:\s*"([^"]+)"""").find(createResponse)?.groupValues?.get(1)
            ?: chatId

        conversationStore.appendTurn(
            createdChatId,
            ConversationTurn(
                turnId = UUID.randomUUID().toString(),
                role = "user",
                text = "Hello export",
                profileId = "hello-world",
                createdAt = Instant.now(),
            ),
        )

        val runId = "run-export-1"
        val now = Instant.now()
        runEventStore.save(
            RunEventRecord(
                eventId = UUID.randomUUID().toString(),
                runId = runId,
                conversationId = createdChatId,
                profileId = "hello-world",
                kind = "run.started",
                runtimeType = "run.started",
                content = emptyMap(),
                createdAt = now,
            ),
        )
        runEventStore.save(
            RunEventRecord(
                eventId = UUID.randomUUID().toString(),
                runId = runId,
                conversationId = createdChatId,
                profileId = "hello-world",
                kind = "tool.call",
                runtimeType = "tool.call",
                content = mapOf(
                    "name" to "say_hello",
                    "arguments" to emptyMap<String, Any?>(),
                    "iteration" to 0,
                ),
                createdAt = now.plusMillis(1),
            ),
        )

        client.get()
            .uri("/api/v1/ai/chats/$createdChatId/scenario-export")
            .accept(MediaType.parseMediaType("application/x-yaml"))
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .value { body ->
                assertThat(body).contains("# source chatId: $createdChatId")
                assertThat(body).contains("ask: \"Hello export\"")
                assertThat(body).contains("say_hello")
                assertThat(body).doesNotContain("\nverify:")
            }
    }

    @Test
    fun shouldReturn404_whenChatMissing() {
        client.get()
            .uri("/api/v1/ai/chats/${UUID.randomUUID()}/scenario-export")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun shouldReturn422_whenChatHasNoUserTurns() {
        val createResponse = client.post()
            .uri("/api/v1/ai/chats")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus().isCreated
            .expectBody(String::class.java)
            .returnResult()
            .responseBody
            ?: error("missing create response")

        val createdChatId = Regex(""""chatId"\s*:\s*"([^"]+)"""").find(createResponse)!!.groupValues[1]

        client.get()
            .uri("/api/v1/ai/chats/$createdChatId/scenario-export")
            .exchange()
            .expectStatus().isEqualTo(422)
    }
}
