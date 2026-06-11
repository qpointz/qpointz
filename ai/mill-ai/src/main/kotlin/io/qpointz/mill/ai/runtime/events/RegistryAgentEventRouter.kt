package io.qpointz.mill.ai.runtime.events

import io.qpointz.mill.ai.core.artifact.ArtifactDescriptor
import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.core.artifact.ArtifactSourceEvent
import io.qpointz.mill.ai.core.artifact.structuredResultMap
import io.qpointz.mill.ai.runtime.events.routing.EventRoutingRule
import io.qpointz.mill.ai.runtime.events.routing.RoutedAgentEvent
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventCategory
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventDestination
import io.qpointz.mill.ai.runtime.events.routing.EventRoute
import java.util.UUID

/**
 * Registry-driven [AgentEventRouter] replacing hardcoded artefact routing tables.
 */
class RegistryAgentEventRouter(
    private val artifactRegistry: ArtifactDescriptorRegistry,
) : AgentEventRouter {

    override fun route(input: AgentEventRoutingInput): List<RoutedAgentEvent> {
        val baseRule = input.policy.ruleFor(input.event.type) ?: return emptyList()
        val rule = refineRule(input.event, baseRule)
        val primary = routedEvent(
            input = input,
            rule = rule,
            content = extractContent(input.event, rule),
        )
        val derived = when (val event = input.event) {
            is AgentEvent.ToolResult -> derivedArtifactEvents(input, event)
            else -> emptyList()
        }
        return listOf(primary) + derived
    }

    private fun refineRule(event: AgentEvent, baseRule: EventRoutingRule): EventRoutingRule =
        when (event) {
            is AgentEvent.ProtocolFinal -> refineProtocolFinal(event, baseRule)
            else -> baseRule
        }

    private fun refineProtocolFinal(
        event: AgentEvent.ProtocolFinal,
        baseRule: EventRoutingRule,
    ): EventRoutingRule {
        val descriptor = artifactRegistry.descriptorForProtocol(event.protocolId) ?: return baseRule
        return descriptor.toArtifactRule(baseRule.eventType).copy(
            destinations = descriptor.destinations.ifEmpty { baseRule.destinations },
        )
    }

    private fun derivedArtifactEvents(
        input: AgentEventRoutingInput,
        event: AgentEvent.ToolResult,
    ): List<RoutedAgentEvent> {
        val resultMap = structuredResultMap(event.result) ?: return emptyList()
        val artifactKind = resultMap["artifactType"] as? String ?: return emptyList()
        val descriptor = artifactRegistry.descriptorForToolResultArtifactKind(artifactKind) ?: return emptyList()
        if (descriptor.sourceEvent != ArtifactSourceEvent.TOOL_RESULT) return emptyList()
        val artifactContent = mapOf(
            "toolName" to event.name,
            "artifactType" to artifactKind,
            "persistKind" to descriptor.persistKind,
            "payload" to resultMap,
        )
        return listOf(
            routedEvent(
                input = input,
                rule = descriptor.toArtifactRule("tool.result"),
                content = artifactContent,
            ),
        )
    }

    private fun ArtifactDescriptor.toArtifactRule(eventType: String): EventRoutingRule =
        EventRoutingRule(
            eventType = eventType,
            kind = persistKind,
            category = RoutedEventCategory.ARTIFACT,
            destinations = destinations,
            persistAsArtifact = destinations.contains(RoutedEventDestination.ARTIFACT),
            artifactPointerKeys = pointerKeys,
        )

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

    private fun extractContent(event: AgentEvent, rule: EventRoutingRule): Map<String, Any?> =
        when (event) {
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
            is AgentEvent.ProtocolFinal -> mapOf(
                "protocolId" to event.protocolId,
                "payload" to event.payload,
                "persistKind" to (artifactRegistry.descriptorForProtocol(event.protocolId)?.persistKind ?: rule.kind),
            )
            is AgentEvent.ProtocolStreamEvent -> mapOf(
                "protocolId" to event.protocolId,
                "eventType" to event.eventType,
                "payload" to event.payload,
            )
            is AgentEvent.LlmCallCompleted -> mapOf(
                "inputTokens" to event.inputTokens,
                "outputTokens" to event.outputTokens,
                "totalTokens" to event.totalTokens,
            )
        }
}
