package io.qpointz.mill.ai.profile

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
 * Minimal profile model for binding a runtime to a capability set.
 *
 * The current hello-world runtime uses a single fixed profile, but the same shape will support
 * future context-specific agents assembled from different capabilities.
 */
data class AgentProfile(
    val id: String,
    val capabilityIds: Set<String>,
    val routingPolicy: EventRoutingPolicy = DefaultEventRoutingPolicy.policy,
)





