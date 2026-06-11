package io.qpointz.mill.ai.runtime.events

import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.runtime.events.routing.RoutedAgentEvent

/**
 * Stateless router that maps a raw [AgentEvent] to one or more [RoutedAgentEvent] envelopes.
 *
 * The router must not hold mutable state; all routing context arrives via [AgentEventRoutingInput].
 */
fun interface AgentEventRouter {
    /** Routes a raw runtime event using the supplied policy and artefact registry context. */
    fun route(input: AgentEventRoutingInput): List<RoutedAgentEvent>
}

/**
 * Default router backed by [RegistryAgentEventRouter] and [ArtifactDescriptorRegistry.loadDefault].
 */
object DefaultAgentEventRouter : AgentEventRouter by RegistryAgentEventRouter(ArtifactDescriptorRegistry.loadDefault())





