package io.qpointz.mill.ai.core.capability

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
 * Marker interface for agent-owned dependency families exposed to capabilities.
 *
 * Concrete examples can include `SchemaFacetDependency` or `SqlExecutorDependency`.
 */
interface CapabilityDependency

/**
 * Typed dependency container passed to a single capability at construction time.
 */
data class CapabilityDependencies(
    private val dependencies: Map<Class<out CapabilityDependency>, CapabilityDependency> = emptyMap(),
) {
    fun <T : CapabilityDependency> get(type: Class<T>): T? = type.cast(dependencies[type])

    fun <T : CapabilityDependency> require(type: Class<T>): T =
        requireNotNull(get(type)) { "Missing capability dependency: ${type.name}" }

    fun contains(type: Class<out CapabilityDependency>): Boolean = dependencies.containsKey(type)

    companion object {
        fun empty(): CapabilityDependencies = CapabilityDependencies()

        fun of(vararg dependencies: CapabilityDependency): CapabilityDependencies =
            CapabilityDependencies(
                dependencies.associateBy { it.javaClass as Class<out CapabilityDependency> }
            )
    }
}

/**
 * Agent-scoped dependency registry keyed by capability id.
 */
data class CapabilityDependencyContainer(
    private val dependenciesByCapabilityId: Map<String, CapabilityDependencies> = emptyMap(),
) {
    fun forCapability(capabilityId: String): CapabilityDependencies =
        dependenciesByCapabilityId[capabilityId] ?: CapabilityDependencies.empty()

    companion object {
        fun empty(): CapabilityDependencyContainer = CapabilityDependencyContainer()

        fun of(vararg entries: Pair<String, CapabilityDependencies>): CapabilityDependencyContainer =
            CapabilityDependencyContainer(entries.toMap())
    }
}





