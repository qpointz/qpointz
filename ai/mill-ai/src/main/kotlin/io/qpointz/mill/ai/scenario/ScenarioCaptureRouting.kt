package io.qpointz.mill.ai.scenario

import io.qpointz.mill.ai.runtime.events.routing.EventRoutingPolicy
import io.qpointz.mill.ai.runtime.events.routing.EventRoutingRule
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventCategory
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventDestination

/**
 * Extends a profile routing policy for scenario capture mode.
 *
 * When [mill.ai.chat.scenario-capture.enabled] is true, tool and protocol events are persisted
 * to `ai_chat_run_event` so live chats can be exported as draft scenario packs.
 */
object ScenarioCaptureRouting {

    /**
     * Returns [base] with capture-friendly persistence overrides applied.
     *
     * @param base Profile or default routing policy.
     */
    fun extendedPolicy(base: EventRoutingPolicy): EventRoutingPolicy {
        val toolCall = base.ruleFor("tool.call")
        val toolResult = base.ruleFor("tool.result")
        val protocolFinal = base.ruleFor("protocol.final")
        return base.overriding(
            toolCall?.copy(persistEvent = true)
                ?: EventRoutingRule(
                    eventType = "tool.call",
                    kind = "tool.call",
                    category = RoutedEventCategory.TELEMETRY,
                    destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.TELEMETRY),
                    persistEvent = true,
                ),
            toolResult?.copy(persistEvent = true)
                ?: EventRoutingRule(
                    eventType = "tool.result",
                    kind = "tool.result",
                    category = RoutedEventCategory.TELEMETRY,
                    destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.TELEMETRY),
                    persistEvent = true,
                ),
            protocolFinal?.copy(persistEvent = true)
                ?: EventRoutingRule(
                    eventType = "protocol.final",
                    kind = "protocol.final",
                    category = RoutedEventCategory.ARTIFACT,
                    destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.ARTIFACT),
                    persistAsArtifact = true,
                    persistEvent = true,
                ),
        )
    }
}
