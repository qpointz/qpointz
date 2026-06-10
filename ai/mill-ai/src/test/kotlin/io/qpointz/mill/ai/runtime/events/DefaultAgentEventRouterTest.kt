package io.qpointz.mill.ai.runtime.events

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
import org.junit.jupiter.api.Test
import java.time.Instant

class DefaultAgentEventRouterTest {

    private val policy = DefaultEventRoutingPolicy.policy

    private fun input(event: AgentEvent) = AgentEventRoutingInput(
        event = event,
        policy = policy,
        conversationId = "conv-1",
        runId = "run-1",
        profileId = "test-profile",
        timestamp = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun shouldProduceRoutedEvent_forKnownEventType() {
        val result = DefaultAgentEventRouter.route(input(AgentEvent.AnswerCompleted("Hello")))
        assertEquals(1, result.size)
        val routed = result.first()
        assertEquals("answer.completed", routed.runtimeType)
        assertEquals("answer.completed", routed.kind)
        assertEquals(RoutedEventCategory.CHAT_TRANSCRIPT, routed.category)
        assertEquals("conv-1", routed.conversationId)
        assertEquals("run-1", routed.runId)
    }

    @Test
    fun shouldDropEvent_whenPolicyHasNoMatchingRule() {
        // Use an empty policy to simulate an event type not in the policy
        val emptyPolicy = EventRoutingPolicy(rules = emptyList())
        val result = DefaultAgentEventRouter.route(
            AgentEventRoutingInput(
                event = AgentEvent.RunStarted("p"),
                policy = emptyPolicy,
                conversationId = "c",
                runId = "r",
                profileId = "p",
            )
        )
        assertTrue(result.isEmpty(), "expected empty result for unregistered event type")
    }

    @Test
    fun shouldExtractContent_forAnswerCompleted() {
        val result = DefaultAgentEventRouter.route(input(AgentEvent.AnswerCompleted("world")))
        assertEquals("world", result.first().content["text"])
    }

    @Test
    fun shouldExtractContent_forLlmCallCompleted() {
        val result = DefaultAgentEventRouter.route(input(AgentEvent.LlmCallCompleted(10, 20, 30)))
        val content = result.first().content
        assertEquals(10, content["inputTokens"])
        assertEquals(20, content["outputTokens"])
        assertEquals(30, content["totalTokens"])
    }

    @Test
    fun shouldPreserveTimestamp() {
        val ts = Instant.parse("2026-03-01T12:00:00Z")
        val result = DefaultAgentEventRouter.route(
            input(AgentEvent.RunStarted("p")).copy(timestamp = ts)
        )
        assertEquals(ts, result.first().createdAt)
    }

    @Test
    fun shouldCarryDestinations_fromRule() {
        val result = DefaultAgentEventRouter.route(input(AgentEvent.AnswerCompleted("x")))
        val dest = result.first().destinations
        assertTrue(dest.contains(RoutedEventDestination.CHAT_STREAM))
        assertTrue(dest.contains(RoutedEventDestination.CHAT_TRANSCRIPT))
        assertTrue(dest.contains(RoutedEventDestination.MODEL_MEMORY))
    }

    @Test
    fun shouldEmitAdditionalArtifactEvent_forCanonicalToolResultArtifact() {
        val result = DefaultAgentEventRouter.route(
            input(
                AgentEvent.ToolResult(
                    name = "validate_sql",
                    result = mapOf(
                        "artifactType" to "sql-validation",
                        "passed" to true,
                        "attempt" to 1,
                        "normalizedSql" to "SELECT 1",
                    ),
                )
            )
        )

        assertEquals(2, result.size)
        val telemetry = result.first { it.kind == "tool.result" }
        val artifact = result.first { it.kind == "sql.validation" }

        assertEquals(RoutedEventCategory.TELEMETRY, telemetry.category)
        assertEquals(RoutedEventCategory.ARTIFACT, artifact.category)
        assertTrue(artifact.destinations.contains(RoutedEventDestination.ARTIFACT))
        assertEquals("sql-validation", artifact.content["artifactType"])
        assertEquals("validate_sql", artifact.content["toolName"])
    }

    @Test
    fun shouldNotEmitArtifactEvent_forNonCanonicalToolResult() {
        val result = DefaultAgentEventRouter.route(
            input(
                AgentEvent.ToolResult(
                    name = "list_tables",
                    result = listOf(mapOf("tableName" to "orders")),
                )
            )
        )

        assertEquals(1, result.size)
        assertEquals("tool.result", result.single().kind)
    }
}





