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

import java.time.Instant

/**
 * Runtime context passed to [AgentEventRouter] for each raw event.
 *
 * The router is stateless; all state needed to produce the routed envelope must be supplied here.
 */
data class AgentEventRoutingInput(
    /** Raw source event to be routed. */
    val event: AgentEvent,
    /** Routing policy to apply; typically resolved from the active [AgentProfile]. */
    val policy: EventRoutingPolicy,
    /** Active conversation id, if any. */
    val conversationId: String?,
    /** Current run id generated per [run()] call. */
    val runId: String?,
    /** Profile that owns this run. */
    val profileId: String,
    /** Optional active transcript turn id, if the runtime has already opened one. */
    val turnId: String? = null,
    /** Wall-clock timestamp; defaults to now if not supplied. */
    val timestamp: Instant = Instant.now(),
)





