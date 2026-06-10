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

/**
 * Receives a single routed event for downstream processing.
 */
fun interface AgentEventListener {
    fun onEvent(event: RoutedAgentEvent)
}

/**
 * Receives a single routed event for projection into a store or derived state.
 */
fun interface RoutedEventProjector {
    fun onEvent(event: RoutedAgentEvent)
}

/**
 * In-process routed event bus.
 *
 * Publish routed events to all registered listeners in registration order.
 * All publishing is synchronous in the first pass.
 */
interface AgentEventPublisher {
    fun publish(event: RoutedAgentEvent)
    fun register(listener: AgentEventListener)
}

/**
 * In-memory, synchronous implementation of [AgentEventPublisher].
 */
class InMemoryAgentEventPublisher : AgentEventPublisher {

    private val listeners: MutableList<AgentEventListener> = mutableListOf()

    override fun publish(event: RoutedAgentEvent) {
        listeners.forEach { it.onEvent(event) }
    }

    override fun register(listener: AgentEventListener) {
        listeners.add(listener)
    }
}





