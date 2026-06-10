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

import java.time.Instant

/**
 * Stable routed envelope for a single agent event.
 *
 * This is the common propagation surface for persistence listeners, chat-stream consumers,
 * and future SSE adapters. Shape is locked per the WI-074 implementation contract.
 */
data class RoutedAgentEvent(
    /** Unique id for this routed event instance. */
    val eventId: String,
    /** Exact value of [AgentEvent.type] from the source event. */
    val runtimeType: String,
    /** Stable routed label for downstream consumers (from the matching [EventRoutingRule]). */
    val kind: String,
    /** Primary coarse classification lane. */
    val category: RoutedEventCategory,
    /** Explicit set of destination lanes; use this — not category — to drive projection decisions. */
    val destinations: Set<RoutedEventDestination>,
    /** Structured, consumer-safe payload extracted from the source event. */
    val content: Map<String, Any?>,
    /** Resolved routing decision including the matched rule. */
    val route: EventRoute,
    /** Conversation this event belongs to, if any. */
    val conversationId: String?,
    /** Single agent run this event belongs to, if any. */
    val runId: String?,
    /** Profile that produced this event. */
    val profileId: String,
    /** Optional transcript turn this event is associated with. */
    val turnId: String? = null,
    /** Wall-clock time the routed event was created. */
    val createdAt: Instant,
)





