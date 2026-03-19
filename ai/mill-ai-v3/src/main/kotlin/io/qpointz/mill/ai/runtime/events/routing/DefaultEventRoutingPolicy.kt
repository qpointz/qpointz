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

/**
 * Default event routing policy built from the locked §6 mapping table in WI-074.
 *
 * Profiles that need different routing should override selected rules via
 * [EventRoutingPolicy] construction rather than subclassing this object.
 */
object DefaultEventRoutingPolicy {

    val policy: EventRoutingPolicy = EventRoutingPolicy(
        rules = listOf(
            EventRoutingRule(
                eventType = "run.started",
                kind = "run.started",
                category = RoutedEventCategory.TELEMETRY,
                destinations = setOf(RoutedEventDestination.TELEMETRY),
                persistEvent = true,
            ),
            EventRoutingRule(
                eventType = "thinking.delta",
                kind = "thinking.delta",
                category = RoutedEventCategory.CHAT_STREAM,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM),
            ),
            EventRoutingRule(
                eventType = "plan.created",
                kind = "plan.created",
                category = RoutedEventCategory.TELEMETRY,
                destinations = setOf(RoutedEventDestination.TELEMETRY),
                persistEvent = true,
            ),
            EventRoutingRule(
                eventType = "message.delta",
                kind = "message.delta",
                category = RoutedEventCategory.CHAT_STREAM,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM),
            ),
            EventRoutingRule(
                eventType = "tool.call",
                kind = "tool.call",
                category = RoutedEventCategory.TELEMETRY,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.TELEMETRY),
            ),
            EventRoutingRule(
                eventType = "tool.result",
                kind = "tool.result",
                category = RoutedEventCategory.TELEMETRY,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.TELEMETRY),
            ),
            EventRoutingRule(
                eventType = "observation.made",
                kind = "observation.made",
                category = RoutedEventCategory.TELEMETRY,
                destinations = setOf(RoutedEventDestination.TELEMETRY),
            ),
            EventRoutingRule(
                eventType = "answer.completed",
                kind = "answer.completed",
                category = RoutedEventCategory.CHAT_TRANSCRIPT,
                destinations = setOf(
                    RoutedEventDestination.CHAT_STREAM,
                    RoutedEventDestination.CHAT_TRANSCRIPT,
                    RoutedEventDestination.MODEL_MEMORY,
                ),
                persistAsTranscript = true,
            ),
            EventRoutingRule(
                eventType = "reasoning.delta",
                kind = "reasoning.delta",
                category = RoutedEventCategory.CHAT_STREAM,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM),
            ),
            EventRoutingRule(
                eventType = "protocol.text.delta",
                kind = "protocol.text.delta",
                category = RoutedEventCategory.CHAT_STREAM,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM),
            ),
            EventRoutingRule(
                eventType = "protocol.final",
                kind = "protocol.final",
                category = RoutedEventCategory.ARTIFACT,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.ARTIFACT),
                persistAsArtifact = true,
            ),
            EventRoutingRule(
                eventType = "protocol.stream.event",
                kind = "protocol.stream.event",
                category = RoutedEventCategory.ARTIFACT,
                destinations = setOf(RoutedEventDestination.CHAT_STREAM, RoutedEventDestination.ARTIFACT),
                persistAsArtifact = false, // profile-specific; default off
            ),
            EventRoutingRule(
                eventType = "llm.call.completed",
                kind = "llm.call.completed",
                category = RoutedEventCategory.TELEMETRY,
                destinations = setOf(RoutedEventDestination.TELEMETRY),
                persistEvent = true,
            ),
        )
    )
}





