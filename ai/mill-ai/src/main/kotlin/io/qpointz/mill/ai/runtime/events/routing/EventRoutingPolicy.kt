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
 * Routing rule for a single event type.
 *
 * Each rule governs how a raw [AgentEvent] with a matching [eventType] is classified
 * and distributed across destination lanes.
 */
data class EventRoutingRule(
    /** Matches the value of [AgentEvent.type]. */
    val eventType: String,
    /** Stable routed label for downstream consumers. */
    val kind: String,
    /** Primary coarse classification lane. */
    val category: RoutedEventCategory,
    /** Explicit set of destination lanes this event is routed to. */
    val destinations: Set<RoutedEventDestination>,
    /** Whether this event should be written to [RunEventStore]. */
    val persistEvent: Boolean = false,
    /** Whether this event should project a canonical transcript turn in [ConversationStore]. */
    val persistAsTranscript: Boolean = false,
    /** Whether this event should be saved as a durable artifact in [ArtifactStore]. */
    val persistAsArtifact: Boolean = false,
    /** Optional pointer keys to update in [ActiveArtifactPointerStore] when the event becomes an artifact. */
    val artifactPointerKeys: Set<String> = emptySet(),
)

/**
 * Explicit routing decision attached to a [RoutedAgentEvent].
 *
 * Carries the resolved [EventRoutingRule] alongside the source event type so
 * downstream projectors have full routing metadata without reaching back into the policy.
 */
data class EventRoute(
    val eventType: String,
    val rule: EventRoutingRule,
)

/**
 * Profile-owned policy that maps raw event types to routing rules.
 *
 * A profile may carry a full policy or rely on the default one.
 */
data class EventRoutingPolicy(
    val rules: List<EventRoutingRule>,
) {
    private val index: Map<String, EventRoutingRule> = rules.associateBy { it.eventType }

    /** Resolves the rule for a given raw event type, or null if no rule is registered. */
    fun ruleFor(eventType: String): EventRoutingRule? = index[eventType]

    /**
     * Returns a new policy with the supplied rules replacing any existing rules with the same
     * [EventRoutingRule.eventType].
     */
    fun overriding(vararg overrides: EventRoutingRule): EventRoutingPolicy {
        val merged = index.toMutableMap()
        overrides.forEach { merged[it.eventType] = it }
        return EventRoutingPolicy(merged.values.toList())
    }
}





