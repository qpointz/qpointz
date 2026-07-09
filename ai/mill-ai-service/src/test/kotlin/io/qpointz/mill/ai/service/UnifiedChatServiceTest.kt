package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.chat.AiChatSettings
import io.qpointz.mill.ai.chat.PropertiesUserIdResolver
import io.qpointz.mill.ai.memory.InMemoryChatMemoryStore
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.InMemoryChatRegistry
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import io.qpointz.mill.ai.profile.PlatformProfiles
import io.qpointz.mill.ai.runtime.TurnContextValues
import io.qpointz.mill.ai.service.dto.AttachExecutionResultHttpRequest
import io.qpointz.mill.ai.service.dto.ExecutionColumnDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.time.Instant

class UnifiedChatServiceTest {

    private val registry = InMemoryChatRegistry()
    private val conversationStore = InMemoryConversationStore()
    private val chatMemoryStore = InMemoryChatMemoryStore()
    private val artifactStore = InMemoryArtifactStore()
    private val runtime = AiV3ChatRuntime { _, message, _ ->
        Flux.just(
            ChatRuntimeEvent.Chunk("echo:$message"),
            ChatRuntimeEvent.Completed("echo:$message"),
        )
    }
    private val service = UnifiedChatService(
        registry = registry,
        conversationStore = conversationStore,
        chatMemoryStore = chatMemoryStore,
        artifactStore = artifactStore,
        profileRegistry = PlatformProfiles.registry(),
        runtime = runtime,
        properties = AiChatSettings(),
        userIdResolver = PropertiesUserIdResolver("default"),
    )

    @Test
    fun `should create general chats and list them`() {
        val result = service.createChat(null)

        assertThat(result.created).isTrue()
        assertThat(result.chat.chatName).isEqualTo("New Chat")
        assertThat(service.listChats()).extracting("chatId").containsExactly(result.chat.chatId)
    }

    @Test
    fun `should exclude contextual chats from listChats`() {
        service.createChat(null)
        service.createChat(CreateChatRequest(contextType = "model", contextId = "x", contextLabel = "X"))

        val listed = service.listChats()
        assertThat(listed).allMatch { it.chatType == "general" }
    }

    @Test
    fun `should reuse contextual chat for same context`() {
        val first = service.createChat(CreateChatRequest(contextType = "model", contextId = "sales.customers", contextLabel = "customers"))
        val second = service.createChat(CreateChatRequest(contextType = "model", contextId = "sales.customers", contextLabel = "customers"))

        assertThat(first.created).isTrue()
        assertThat(second.created).isFalse()
        assertThat(second.chat.chatId).isEqualTo(first.chat.chatId)
        assertThat(service.getChatByContext("model", "sales.customers")?.chatId).isEqualTo(first.chat.chatId)
    }

    @Test
    fun `should reject unknown profile on create`() {
        assertThatThrownBy {
            service.createChat(CreateChatRequest(profileId = "missing-profile"))
        }.isInstanceOf(InvalidChatUpdateException::class.java)
            .hasMessageContaining("Unknown profile")
    }

    @Test
    fun `should create analysis contextual chat with analysis-copilot profile`() {
        val result = service.createChat(
            CreateChatRequest(
                profileId = "analysis-copilot",
                contextType = "analysis",
                contextId = "__analysis__",
                contextLabel = "Analysis",
            ),
        )

        assertThat(result.created).isTrue()
        assertThat(result.chat.profileId).isEqualTo("analysis-copilot")
    }

    @Test
    fun `should forward turn context to runtime on send`() {
        val captured = mutableListOf<TurnContextValues?>()
        val runtimeWithContext = AiV3ChatRuntime { _, message, turnContext ->
            captured.add(turnContext)
            Flux.just(ChatRuntimeEvent.Completed("ok:$message"))
        }
        val contextualService = UnifiedChatService(
            registry = registry,
            conversationStore = conversationStore,
            chatMemoryStore = chatMemoryStore,
            artifactStore = artifactStore,
            profileRegistry = PlatformProfiles.registry(),
            runtime = runtimeWithContext,
            properties = AiChatSettings(),
            userIdResolver = PropertiesUserIdResolver("default"),
        )
        val chat = contextualService.createChat(
            CreateChatRequest(
                profileId = "analysis-copilot",
                contextType = "analysis",
                contextId = "__analysis__",
                contextLabel = "Analysis",
            ),
        ).chat

        contextualService.sendMessage(
            chat.chatId,
            "optimize",
            TurnContextValues(values = mapOf("sql.current" to "SELECT 1")),
        ).blockLast()

        assertThat(captured).hasSize(1)
        assertThat(captured[0]?.stringValue("sql.current")).isEqualTo("SELECT 1")
    }

    @Test
    fun `should derive general chat title from first user message`() {
        val chat = service.createChat(null).chat

        val events = service.sendMessage(chat.chatId, "How do I expose a unified chat API?")
            .collectList()
            .block()!!

        assertThat(events).hasSize(2)
        assertThat(service.getChat(chat.chatId)?.chat?.chatName).isEqualTo("How do I expose a unified chat...")
    }

    @Test
    fun `should delete chats from registry`() {
        val chat = service.createChat(null).chat

        assertThat(service.deleteChat(chat.chatId)).isTrue()
        assertThat(service.getChat(chat.chatId)).isNull()
    }

    @Test
    fun `should include artefacts on getChat replay`() {
        val chat = service.createChat(null).chat
        conversationStore.ensureExists(chat.chatId, chat.profileId)
        val turnId = "turn-1"
        conversationStore.appendTurn(
            chat.chatId,
            ConversationTurn(
                turnId = turnId,
                role = "assistant",
                text = "Here is SQL",
                artifactIds = emptyList(),
                profileId = chat.profileId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            ),
        )
        val artifactId = "art-1"
        artifactStore.save(
            ArtifactRecord(
                artifactId = artifactId,
                conversationId = chat.chatId,
                runId = "run-1",
                kind = "sql-query.generated-sql",
                payload = mapOf(
                    "protocolId" to "sql-query.generated-sql",
                    "payload" to mapOf("artifactType" to "generated-sql", "sql" to "SELECT 1"),
                ),
                turnId = turnId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            ),
        )
        conversationStore.attachArtifacts(chat.chatId, turnId, listOf(artifactId))

        val view = service.getChat(chat.chatId)!!
        assertThat(view.messages).hasSize(1)
        assertThat(view.messages[0].artifacts).hasSize(1)
        assertThat(view.messages[0].artifacts[0].kind).isEqualTo("sql")
        assertThat(view.messages[0].assistantReplyView).isNull()
    }

    @Test
    fun `should resolve artefacts on getChat replay when turn relation missing but artifact turnId set`() {
        val chat = service.createChat(null).chat
        conversationStore.ensureExists(chat.chatId, chat.profileId)
        val turnId = "turn-missing-relation"
        conversationStore.appendTurn(
            chat.chatId,
            ConversationTurn(
                turnId = turnId,
                role = "assistant",
                text = null,
                artifactIds = emptyList(),
                profileId = chat.profileId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            ),
        )
        artifactStore.save(
            ArtifactRecord(
                artifactId = "art-orphan",
                conversationId = chat.chatId,
                runId = "run-1",
                kind = "sql.generated",
                payload = mapOf(
                    "artifactType" to "generated-sql",
                    "sql" to "SELECT month FROM sales",
                    "dialectId" to "CALCITE",
                ),
                turnId = turnId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            ),
        )

        val view = service.getChat(chat.chatId)!!
        assertThat(view.messages.single().artifacts).hasSize(1)
        assertThat(view.messages.single().artifacts[0].kind).isEqualTo("sql")
    }

    @Test
    fun `should include facet-proposal on getChat replay`() {
        val chat = service.createChat(null).chat
        conversationStore.ensureExists(chat.chatId, chat.profileId)
        val turnId = "turn-facet"
        conversationStore.appendTurn(
            chat.chatId,
            ConversationTurn(
                turnId = turnId,
                role = "assistant",
                text = null,
                artifactIds = emptyList(),
                profileId = chat.profileId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            ),
        )
        val artifactId = "art-facet"
        artifactStore.save(
            ArtifactRecord(
                artifactId = artifactId,
                conversationId = chat.chatId,
                runId = "run-1",
                kind = "metadata.faceting.capture",
                payload = mapOf(
                    "protocolId" to "metadata.faceting.capture",
                    "persistKind" to "metadata.faceting.capture",
                    "payload" to mapOf(
                        "captureType" to "facet_assignment",
                        "facetTypeKey" to "descriptive",
                        "metadataEntityId" to "sales.customers",
                        "serializedPayload" to mapOf("summary" to "VIP customer segment"),
                        "validationWarnings" to emptyList<String>(),
                    ),
                ),
                turnId = turnId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            ),
        )
        conversationStore.attachArtifacts(chat.chatId, turnId, listOf(artifactId))

        val view = service.getChat(chat.chatId)!!
        assertThat(view.messages).hasSize(1)
        assertThat(view.messages[0].artifacts).hasSize(1)
        assertThat(view.messages[0].artifacts[0].kind).isEqualTo("facet-proposal")
        assertThat(view.messages[0].assistantReplyView).isNull()
    }

    @Test
    fun `should attach execution result metadata`() {
        val chat = service.createChat(null).chat
        conversationStore.ensureExists(chat.chatId, chat.profileId)
        val turnId = "turn-2"
        conversationStore.appendTurn(
            chat.chatId,
            ConversationTurn(
                turnId = turnId,
                role = "assistant",
                text = null,
                artifactIds = emptyList(),
                profileId = chat.profileId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            ),
        )

        val attached = service.attachExecutionResult(
            chat.chatId,
            turnId,
            AttachExecutionResultHttpRequest(
                executionId = "exec-99",
                columns = listOf(ExecutionColumnDto("id", "int")),
                rowCount = 10,
                truncated = false,
                sql = "SELECT id FROM t",
                parentArtifactId = "sql-parent",
            ),
        )

        assertThat(attached?.kind).isEqualTo("data")
        assertThat(attached?.payload).doesNotContainKey("executionId")
        assertThat(attached?.payload).doesNotContainKey("resultId")
        assertThat(attached?.payload?.get("sql")).isEqualTo("SELECT id FROM t")
        assertThat(attached?.payload?.get("sourceArtifactId")).isEqualTo("sql-parent")
        val replay = service.getChat(chat.chatId)!!.messages.single()
        val data = replay.artifacts.single { it.kind == "data" }
        assertThat(data.payload).doesNotContainKey("executionId")
    }

    @Test
    fun `should update profile on general chat and sync conversation store`() {
        val chat = service.createChat(null).chat
        conversationStore.ensureExists(chat.chatId, chat.profileId)

        val updated = service.updateChat(chat.chatId, ChatUpdate(profileId = "data-analysis"))!!

        assertThat(updated.profileId).isEqualTo("data-analysis")
        assertThat(conversationStore.load(chat.chatId)!!.profileId).isEqualTo("data-analysis")
    }

    @Test
    fun `should reject unknown profile on update`() {
        val chat = service.createChat(null).chat

        assertThatThrownBy {
            service.updateChat(chat.chatId, ChatUpdate(profileId = "no-such-profile"))
        }.isInstanceOf(InvalidChatUpdateException::class.java)
            .hasMessageContaining("Unknown profile")
    }

    @Test
    fun `should reject profile change on contextual chat`() {
        val chat = service.createChat(
            CreateChatRequest(contextType = "model", contextId = "sales.customers", contextLabel = "customers"),
        ).chat

        assertThatThrownBy {
            service.updateChat(chat.chatId, ChatUpdate(profileId = "data-analysis"))
        }.isInstanceOf(InvalidChatUpdateException::class.java)
            .hasMessageContaining("contextual")
    }

    @Test
    fun `should hide chat from other users`() {
        val chat = service.createChat(null).chat
        val otherUserService = UnifiedChatService(
            registry = registry,
            conversationStore = conversationStore,
            chatMemoryStore = chatMemoryStore,
            artifactStore = artifactStore,
            profileRegistry = PlatformProfiles.registry(),
            runtime = runtime,
            properties = AiChatSettings(),
            userIdResolver = PropertiesUserIdResolver("other-user"),
        )

        assertThat(otherUserService.getChat(chat.chatId)).isNull()
        assertThat(otherUserService.updateChat(chat.chatId, ChatUpdate(chatName = "nope"))).isNull()
        assertThat(otherUserService.deleteChat(chat.chatId)).isFalse()
    }
}
