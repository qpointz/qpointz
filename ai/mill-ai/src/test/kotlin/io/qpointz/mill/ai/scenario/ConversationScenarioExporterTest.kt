package io.qpointz.mill.ai.scenario

import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ConversationRecord
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.InMemoryChatRegistry
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import io.qpointz.mill.ai.persistence.InMemoryRunEventStore
import io.qpointz.mill.ai.persistence.RunEventRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ConversationScenarioExporterTest {

    private val registry = InMemoryChatRegistry()
    private val conversationStore = InMemoryConversationStore()
    private val runEventStore = InMemoryRunEventStore()
    private val artifactStore = InMemoryArtifactStore()
    private val exporter = ConversationScenarioExporter(
        chatRegistry = registry,
        conversationStore = conversationStore,
        runEventStore = runEventStore,
        artifactStore = artifactStore,
    )

    @Test
    fun shouldExportAskAndScriptFromCapturedToolEvents() {
        seedChat("chat-1", "data-analysis")
        appendUserTurn("chat-1", "Show revenue by month", "data-analysis")

        val runId = "run-1"
        val now = Instant.parse("2026-06-25T10:00:00Z")
        runEventStore.save(runEvent(runId, "chat-1", "run.started", now))
        runEventStore.save(
            runEvent(
                runId,
                "chat-1",
                "tool.call",
                now.plusSeconds(1),
                mapOf(
                    "name" to "validate_sql",
                    "arguments" to mapOf("sql" to "SELECT 1", "attempt" to 1),
                    "iteration" to 0,
                ),
            ),
        )

        val export = exporter.export("chat-1", exportedAt = now)!!
        assertThat(export.pack.name).isEqualTo("export-test")
        assertThat(export.pack.profileId).isEqualTo("data-analysis")
        assertThat(export.pack.parameters.mode).isEqualTo("scripted")
        assertThat(export.pack.run).hasSize(1)
        assertThat(export.pack.run[0].ask).isEqualTo("Show revenue by month")
        assertThat(export.pack.run[0].script).hasSize(1)
        assertThat(export.pack.run[0].script!![0].toolCalls!![0].name).isEqualTo("validate_sql")
        assertThat(export.pack.run[0].verify).isNull()

        val yaml = ScenarioPackYamlWriter().write(export)
        assertThat(yaml).contains("# source chatId: chat-1")
        assertThat(yaml).contains("validate_sql")
        assertThat(yaml).doesNotContain("\nverify:")
    }

    @Test
    fun shouldFallbackToArtifactHeuristic_whenToolEventsMissing() {
        seedChat("chat-2", "data-analysis")
        appendUserTurn("chat-2", "Show sales", "data-analysis")

        artifactStore.save(
            ArtifactRecord(
                artifactId = "a1",
                conversationId = "chat-2",
                runId = "run-x",
                kind = "sql.generated",
                payload = mapOf("sql" to "SELECT region FROM sales"),
                createdAt = Instant.now(),
            ),
        )

        val export = exporter.export("chat-2")!!
        assertThat(export.pack.run[0].script).hasSize(1)
        assertThat(export.pack.run[0].script!![0].toolCalls!![0].name).isEqualTo("validate_sql")
        assertThat(export.verifyHints).anyMatch { it.contains("sql.generated") }
    }

    private fun seedChat(chatId: String, profileId: String) {
        registry.create(
            ChatMetadata(
                chatId = chatId,
                userId = "user-1",
                profileId = profileId,
                chatName = "export-test",
                chatType = "general",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
        )
        conversationStore.ensureExists(chatId, profileId)
    }

    private fun appendUserTurn(chatId: String, text: String, profileId: String) {
        conversationStore.appendTurn(
            chatId,
            ConversationTurn(
                turnId = "turn-user-1",
                role = "user",
                text = text,
                profileId = profileId,
                createdAt = Instant.now(),
            ),
        )
    }

    private fun runEvent(
        runId: String,
        chatId: String,
        kind: String,
        createdAt: Instant,
        content: Map<String, Any?> = emptyMap(),
    ): RunEventRecord = RunEventRecord(
        eventId = "${runId}-${kind}-${createdAt.epochSecond}",
        runId = runId,
        conversationId = chatId,
        profileId = "data-analysis",
        kind = kind,
        runtimeType = kind,
        content = content,
        createdAt = createdAt,
    )
}
