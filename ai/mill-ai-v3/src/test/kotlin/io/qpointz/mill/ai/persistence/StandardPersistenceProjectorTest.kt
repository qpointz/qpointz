package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class StandardPersistenceProjectorTest {

    private lateinit var runEventStore: InMemoryRunEventStore
    private lateinit var conversationStore: InMemoryConversationStore
    private lateinit var artifactStore: InMemoryArtifactStore
    private lateinit var pointerStore: InMemoryActiveArtifactPointerStore
    private lateinit var observerMessages: MutableList<String>
    private lateinit var projector: StandardPersistenceProjector

    @BeforeEach
    fun setUp() {
        runEventStore = InMemoryRunEventStore()
        conversationStore = InMemoryConversationStore()
        artifactStore = InMemoryArtifactStore()
        pointerStore = InMemoryActiveArtifactPointerStore()
        observerMessages = mutableListOf()
        projector = StandardPersistenceProjector(
            runEventStore,
            conversationStore,
            artifactStore,
            pointerStore,
            artifactObservers = listOf(NoOpArtifactObserver(observerMessages::add)),
        )
    }

    private fun routedEvent(
        eventType: String,
        rule: EventRoutingRule,
        content: Map<String, Any?> = emptyMap(),
        conversationId: String? = "conv-1",
        runId: String? = "run-1",
        turnId: String? = UUID.randomUUID().toString(),
    ) = RoutedAgentEvent(
        eventId = UUID.randomUUID().toString(),
        runtimeType = eventType,
        kind = rule.kind,
        category = rule.category,
        destinations = rule.destinations,
        content = content,
        route = EventRoute(eventType, rule),
        conversationId = conversationId,
        runId = runId,
        profileId = "test",
        turnId = turnId,
        createdAt = Instant.now(),
    )

    @Test
    fun shouldPersistRunEvent_whenFlagSet() {
        val rule = DefaultEventRoutingPolicy.policy.ruleFor("run.started")!!
        projector.onEvent(routedEvent("run.started", rule))
        assertEquals(1, runEventStore.findByRun("run-1").size)
    }

    @Test
    fun shouldNotPersistRunEvent_whenFlagFalse() {
        val rule = DefaultEventRoutingPolicy.policy.ruleFor("message.delta")!!
        projector.onEvent(routedEvent("message.delta", rule))
        assertTrue(runEventStore.findByRun("run-1").isEmpty())
    }

    @Test
    fun shouldPersistTranscriptTurn_forAnswerCompleted() {
        conversationStore.ensureExists("conv-1", "p")
        val rule = DefaultEventRoutingPolicy.policy.ruleFor("answer.completed")!!
        projector.onEvent(routedEvent("answer.completed", rule, content = mapOf("text" to "Hello")))
        val record = conversationStore.load("conv-1")!!
        assertEquals(1, record.turns.size)
        assertEquals("assistant", record.turns[0].role)
        assertEquals("Hello", record.turns[0].text)
    }

    @Test
    fun shouldPersistArtifact_forProtocolFinal() {
        val rule = DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")!!
        projector.onEvent(routedEvent(
            "protocol.final", rule,
            content = mapOf("protocolId" to "sql-query", "payload" to "SELECT 1"),
        ))
        assertEquals(1, artifactStore.findByRun("run-1").size)
        assertEquals("sql-query", artifactStore.findByRun("run-1").first().kind)
    }

    @Test
    fun shouldUpdatePointer_whenArtifactPointerKeySet() {
        val rule = EventRoutingRule(
            eventType = "protocol.final",
            kind = "protocol.final",
            category = RoutedEventCategory.ARTIFACT,
            destinations = setOf(RoutedEventDestination.ARTIFACT),
            persistAsArtifact = true,
            artifactPointerKeys = setOf("last-sql"),
        )
        projector.onEvent(routedEvent("protocol.final", rule, content = mapOf("protocolId" to "sql-query")))
        val pointer = pointerStore.find("conv-1", "last-sql")
        assertEquals("conv-1", pointer?.conversationId)
    }

    @Test
    fun shouldSkipPersistence_whenRunIdMissing_forRunEvent() {
        val rule = DefaultEventRoutingPolicy.policy.ruleFor("run.started")!!
        projector.onEvent(routedEvent("run.started", rule, runId = null))
        assertTrue(runEventStore.findByRun("run-1").isEmpty())
    }

    @Test
    fun shouldLinkArtifacts_inTranscriptTurn_whenArtifactPersistedFirst() {
        val turnId = UUID.randomUUID().toString()
        conversationStore.ensureExists("conv-1", "p")

        // Persist artifact first (capture path: ProtocolFinal before AnswerCompleted)
        val artifactRule = DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")!!
        projector.onEvent(routedEvent(
            "protocol.final", artifactRule,
            content = mapOf("protocolId" to "sql-query", "payload" to mapOf("sql" to "SELECT 1")),
            turnId = turnId,
        ))

        // Then create the transcript turn with the same turnId
        val transcriptRule = DefaultEventRoutingPolicy.policy.ruleFor("answer.completed")!!
        projector.onEvent(routedEvent(
            "answer.completed", transcriptRule,
            content = mapOf("text" to ""),
            turnId = turnId,
        ))

        val record = conversationStore.load("conv-1")!!
        assertEquals(1, record.turns.size)
        assertEquals(1, record.turns[0].artifactIds.size)
    }

    @Test
    fun shouldLinkArtifacts_whenTranscriptTurnExistsBeforeArtifact() {
        val turnId = UUID.randomUUID().toString()
        conversationStore.ensureExists("conv-1", "p")

        val transcriptRule = DefaultEventRoutingPolicy.policy.ruleFor("answer.completed")!!
        projector.onEvent(routedEvent(
            "answer.completed", transcriptRule,
            content = mapOf("text" to "answer"),
            turnId = turnId,
        ))

        val artifactRule = DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")!!
        projector.onEvent(routedEvent(
            "protocol.final", artifactRule,
            content = mapOf("protocolId" to "sql-query.generated-sql", "payload" to mapOf("sql" to "SELECT 1")),
            turnId = turnId,
        ))

        val turn = conversationStore.load("conv-1")!!.turns.single()
        assertEquals(listOf(artifactStore.findByRun("run-1").single().artifactId), turn.artifactIds)
    }

    @Test
    fun shouldLinkMultipleArtifacts_toSingleTranscriptTurn() {
        val turnId = UUID.randomUUID().toString()
        conversationStore.ensureExists("conv-1", "p")

        val transcriptRule = DefaultEventRoutingPolicy.policy.ruleFor("answer.completed")!!
        projector.onEvent(routedEvent(
            "answer.completed", transcriptRule,
            content = mapOf("text" to "Here is the chart."),
            turnId = turnId,
        ))

        val artifactRule = DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")!!
        projector.onEvent(routedEvent(
            "protocol.final", artifactRule,
            content = mapOf(
                "protocolId" to "sql-query.generated-sql",
                "payload" to mapOf("artifactType" to "generated-sql", "sql" to "SELECT month, revenue FROM sales"),
            ),
            turnId = turnId,
        ))
        projector.onEvent(routedEvent(
            "protocol.final", artifactRule,
            content = mapOf(
                "protocolId" to "chart-config",
                "payload" to mapOf("artifactType" to "chart-config", "type" to "bar", "x" to "month", "y" to "revenue"),
            ),
            turnId = turnId,
        ))

        val artifacts = artifactStore.findByRun("run-1")
        val turn = conversationStore.load("conv-1")!!.turns.single()
        assertEquals(2, artifacts.size)
        assertEquals(2, turn.artifactIds.size)
        assertTrue(turn.artifactIds.containsAll(artifacts.map { it.artifactId }))
    }

    @Test
    fun shouldProduceNullText_forEmptyAnswerOnCapturePath() {
        conversationStore.ensureExists("conv-1", "p")
        val rule = DefaultEventRoutingPolicy.policy.ruleFor("answer.completed")!!
        projector.onEvent(routedEvent("answer.completed", rule, content = mapOf("text" to "")))
        val turn = conversationStore.load("conv-1")!!.turns[0]
        assertEquals(null, turn.text)
    }

    @Test
    fun shouldNotifyArtifactObserver_afterArtifactPersistence() {
        val rule = DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")!!
        projector.onEvent(
            routedEvent(
                "protocol.final",
                rule,
                content = mapOf(
                    "protocolId" to "sql-query.generated-sql",
                    "payload" to mapOf("artifactType" to "generated-sql", "sql" to "SELECT 1"),
                ),
            )
        )
        assertEquals(1, artifactStore.findByRun("run-1").size)
        assertEquals(1, observerMessages.size)
        assertTrue(observerMessages.single().contains("artifactType=generated-sql"))
    }
}





