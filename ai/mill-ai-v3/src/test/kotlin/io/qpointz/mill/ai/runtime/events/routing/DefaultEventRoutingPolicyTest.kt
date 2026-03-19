package io.qpointz.mill.ai.runtime.events.routing

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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DefaultEventRoutingPolicyTest {

    private val policy = DefaultEventRoutingPolicy.policy

    @Test
    fun shouldRouteAnswerCompleted_toTranscriptAndModelMemory() {
        val rule = policy.ruleFor("answer.completed")!!
        assertEquals(RoutedEventCategory.CHAT_TRANSCRIPT, rule.category)
        assertTrue(rule.destinations.contains(RoutedEventDestination.CHAT_TRANSCRIPT))
        assertTrue(rule.destinations.contains(RoutedEventDestination.MODEL_MEMORY))
        assertTrue(rule.persistAsTranscript)
    }

    @Test
    fun shouldRouteProtocolFinal_toArtifact() {
        val rule = policy.ruleFor("protocol.final")!!
        assertEquals(RoutedEventCategory.ARTIFACT, rule.category)
        assertTrue(rule.destinations.contains(RoutedEventDestination.ARTIFACT))
        assertTrue(rule.persistAsArtifact)
    }

    @Test
    fun shouldRouteRunStarted_toTelemetry_andPersist() {
        val rule = policy.ruleFor("run.started")!!
        assertEquals(RoutedEventCategory.TELEMETRY, rule.category)
        assertTrue(rule.persistEvent)
    }

    @Test
    fun shouldRouteMessageDelta_toChatStreamOnly() {
        val rule = policy.ruleFor("message.delta")!!
        assertEquals(RoutedEventCategory.CHAT_STREAM, rule.category)
        assertEquals(setOf(RoutedEventDestination.CHAT_STREAM), rule.destinations)
        assertTrue(!rule.persistAsTranscript)
    }

    @Test
    fun shouldRouteLlmCallCompleted_toTelemetry() {
        val rule = policy.ruleFor("llm.call.completed")!!
        assertEquals(RoutedEventCategory.TELEMETRY, rule.category)
        assertTrue(rule.persistEvent)
    }

    @Test
    fun shouldCoverAllKnownEventTypes() {
        val known = listOf(
            "run.started", "thinking.delta", "plan.created", "message.delta",
            "tool.call", "tool.result", "observation.made", "answer.completed",
            "reasoning.delta", "protocol.text.delta", "protocol.final",
            "protocol.stream.event", "llm.call.completed",
        )
        known.forEach { type ->
            assertNotNull(policy.ruleFor(type), "missing rule for $type")
        }
    }

    @Test
    fun shouldAllowProfileOverride_forArtifactPointers() {
        val rule = SchemaAuthoringAgentProfile.profile.routingPolicy.ruleFor("protocol.final")!!
        assertEquals(setOf("last-schema-capture"), rule.artifactPointerKeys)
    }
}





