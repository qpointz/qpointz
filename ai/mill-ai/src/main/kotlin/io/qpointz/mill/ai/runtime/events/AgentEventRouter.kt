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

import java.util.UUID

/**
 * Stateless router that maps a raw [AgentEvent] to one or more [RoutedAgentEvent] envelopes.
 *
 * The router must not hold mutable state; all routing context arrives via [AgentEventRoutingInput].
 */
fun interface AgentEventRouter {
    fun route(input: AgentEventRoutingInput): List<RoutedAgentEvent>
}

/**
 * Default router implementation.
 *
 * Looks up the matching rule from the policy and produces a single routed envelope.
 * If no rule is registered for an event type the event is dropped silently.
 */
object DefaultAgentEventRouter : AgentEventRouter {

    override fun route(input: AgentEventRoutingInput): List<RoutedAgentEvent> {
        val baseRule = input.policy.ruleFor(input.event.type) ?: return emptyList()
        val rule = refineStructuredFinalPointers(input.event, baseRule)
        val primary = routedEvent(
            input = input,
            rule = rule,
            content = extractContent(input.event),
        )
        val derived = when (val event = input.event) {
            is AgentEvent.ToolResult -> derivedArtifactEvents(input, event)
            else -> emptyList()
        }
        return listOf(primary) + derived
    }


    internal val STRUCTURED_FINAL_ARTIFACT_POINTER_KEYS: Map<String, Set<String>> = mapOf(
        "schema-authoring.capture" to setOf("last-schema-capture"),
        "metadata.faceting.capture" to setOf("last-metadata-facet-proposal"),
    )

    internal fun refineStructuredFinalPointers(event: AgentEvent, baseRule: EventRoutingRule): EventRoutingRule {
        if (event !is AgentEvent.ProtocolFinal) return baseRule
        val keys = STRUCTURED_FINAL_ARTIFACT_POINTER_KEYS[event.protocolId] ?: return baseRule
        return baseRule.copy(artifactPointerKeys = keys)
    }
    private fun routedEvent(
        input: AgentEventRoutingInput,
        rule: EventRoutingRule,
        content: Map<String, Any?>,
    ): RoutedAgentEvent = RoutedAgentEvent(
        eventId = UUID.randomUUID().toString(),
        runtimeType = input.event.type,
        kind = rule.kind,
        category = rule.category,
        destinations = rule.destinations,
        content = content,
        route = EventRoute(eventType = input.event.type, rule = rule),
        conversationId = input.conversationId,
        runId = input.runId,
        profileId = input.profileId,
        turnId = input.turnId,
        createdAt = input.timestamp,
    )

    private fun derivedArtifactEvents(
        input: AgentEventRoutingInput,
        event: AgentEvent.ToolResult,
    ): List<RoutedAgentEvent> {
        val resultMap = event.result as? Map<*, *> ?: return emptyList()
        val artifactType = resultMap["artifactType"] as? String ?: return emptyList()
        val artifactRule = canonicalArtifactRule(artifactType) ?: return emptyList()
        val artifactContent = mapOf(
            "toolName" to event.name,
            "artifactType" to artifactType,
            "payload" to resultMap,
        )
        return listOf(
            routedEvent(
                input = input,
                rule = artifactRule,
                content = artifactContent,
            )
        )
    }

    private fun canonicalArtifactRule(artifactType: String): EventRoutingRule? = when (artifactType) {
        "generated-sql" -> EventRoutingRule(
            eventType = "tool.result",
            kind = "sql.generated",
            category = RoutedEventCategory.ARTIFACT,
            destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.ARTIFACT),
            persistAsArtifact = true,
            artifactPointerKeys = setOf("last-sql"),
        )
        // Host-injected execution metadata (not produced by the sql-query capability tool loop).
        "sql-result" -> EventRoutingRule(
            eventType = "tool.result",
            kind = "sql.result",
            category = RoutedEventCategory.ARTIFACT,
            destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.ARTIFACT),
            persistAsArtifact = true,
            artifactPointerKeys = setOf("last-sql-result"),
        )
        "sql-validation" -> EventRoutingRule(
            eventType = "tool.result",
            kind = "sql.validation",
            category = RoutedEventCategory.ARTIFACT,
            destinations = setOf(RoutedEventDestination.ARTIFACT),
            persistAsArtifact = true,
        )
        else -> null
    }

    private fun extractContent(event: AgentEvent): Map<String, Any?> = when (event) {
        is AgentEvent.RunStarted -> mapOf("profileId" to event.profileId)
        is AgentEvent.ThinkingDelta -> mapOf("message" to event.message)
        is AgentEvent.PlanCreated -> mapOf("mode" to event.mode, "toolName" to event.toolName)
        is AgentEvent.MessageDelta -> mapOf("text" to event.text)
        is AgentEvent.ToolCall -> mapOf("name" to event.name, "arguments" to event.arguments, "iteration" to event.iteration)
        is AgentEvent.ToolResult -> mapOf("name" to event.name, "result" to event.result)
        is AgentEvent.ObservationMade -> mapOf("decision" to event.decision, "reason" to event.reason)
        is AgentEvent.AnswerCompleted -> mapOf("text" to event.text)
        is AgentEvent.ReasoningDelta -> mapOf("text" to event.text)
        is AgentEvent.ProtocolTextDelta -> mapOf("protocolId" to event.protocolId, "text" to event.text)
        is AgentEvent.ProtocolFinal -> mapOf("protocolId" to event.protocolId, "payload" to event.payload)
        is AgentEvent.ProtocolStreamEvent -> mapOf("protocolId" to event.protocolId, "eventType" to event.eventType, "payload" to event.payload)
        is AgentEvent.LlmCallCompleted -> mapOf(
            "inputTokens" to event.inputTokens,
            "outputTokens" to event.outputTokens,
            "totalTokens" to event.totalTokens,
        )
    }
}





