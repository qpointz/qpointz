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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class RunTelemetryAccumulatorTest {

    private val accumulator = RunTelemetryAccumulator()
    private val policy = DefaultEventRoutingPolicy.policy

    private fun routedFrom(event: AgentEvent, runId: String = "run-1"): RoutedAgentEvent {
        val rule = policy.ruleFor(event.type) ?: error("no rule for ${event.type}")
        return RoutedAgentEvent(
            eventId = UUID.randomUUID().toString(),
            runtimeType = event.type,
            kind = rule.kind,
            category = rule.category,
            destinations = rule.destinations,
            content = when (event) {
                is AgentEvent.LlmCallCompleted -> mapOf(
                    "inputTokens" to event.inputTokens,
                    "outputTokens" to event.outputTokens,
                    "totalTokens" to event.totalTokens,
                )
                else -> emptyMap()
            },
            route = EventRoute(event.type, rule),
            conversationId = "c1",
            runId = runId,
            profileId = "test",
            createdAt = Instant.now(),
        )
    }

    @Test
    fun shouldReturnNull_forUnknownRun() {
        assertNull(accumulator.statsFor("none"))
    }

    @Test
    fun shouldAccumulateTokens_fromLlmCallCompleted() {
        accumulator.onEvent(routedFrom(AgentEvent.LlmCallCompleted(10, 5, 15)))
        val stats = accumulator.statsFor("run-1")!!
        assertEquals(10, stats.inputTokens)
        assertEquals(5, stats.outputTokens)
        assertEquals(15, stats.totalTokens)
    }

    @Test
    fun shouldAccumulateTokens_acrossMultipleCalls() {
        accumulator.onEvent(routedFrom(AgentEvent.LlmCallCompleted(10, 5, 15)))
        accumulator.onEvent(routedFrom(AgentEvent.LlmCallCompleted(20, 8, 28)))
        val stats = accumulator.statsFor("run-1")!!
        assertEquals(30, stats.inputTokens)
        assertEquals(13, stats.outputTokens)
        assertEquals(43, stats.totalTokens)
    }

    @Test
    fun shouldCountToolCalls() {
        val toolCallRule = policy.ruleFor("tool.call")!!
        val event = RoutedAgentEvent(
            eventId = UUID.randomUUID().toString(),
            runtimeType = "tool.call",
            kind = toolCallRule.kind,
            category = toolCallRule.category,
            destinations = toolCallRule.destinations,
            content = mapOf("name" to "list_schemas", "arguments" to emptyMap<String, Any?>(), "iteration" to 0),
            route = EventRoute("tool.call", toolCallRule),
            conversationId = "c1",
            runId = "run-1",
            profileId = "test",
            createdAt = Instant.now(),
        )
        accumulator.onEvent(event)
        accumulator.onEvent(event)
        assertEquals(2, accumulator.statsFor("run-1")!!.toolCallCount)
    }

    @Test
    fun shouldIsolateStats_perRun() {
        accumulator.onEvent(routedFrom(AgentEvent.LlmCallCompleted(10, 5, 15), runId = "run-A"))
        accumulator.onEvent(routedFrom(AgentEvent.LlmCallCompleted(20, 8, 28), runId = "run-B"))
        assertEquals(10, accumulator.statsFor("run-A")!!.inputTokens)
        assertEquals(20, accumulator.statsFor("run-B")!!.inputTokens)
    }

    @Test
    fun shouldIgnoreNonTelemetryEvents() {
        val transcriptRule = policy.ruleFor("answer.completed")!!
        val event = RoutedAgentEvent(
            eventId = UUID.randomUUID().toString(),
            runtimeType = "answer.completed",
            kind = transcriptRule.kind,
            category = transcriptRule.category,
            destinations = transcriptRule.destinations,
            content = mapOf("text" to "hello"),
            route = EventRoute("answer.completed", transcriptRule),
            conversationId = "c1",
            runId = "run-1",
            profileId = "test",
            createdAt = Instant.now(),
        )
        accumulator.onEvent(event)
        assertNull(accumulator.statsFor("run-1"))
    }
}





