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
 * Factory boundary used by runtime discovery.
 *
 * ServiceLoader discovers providers and the runtime asks them to build context-specific
 * capability instances.
 */
interface CapabilityProvider {
    fun descriptor(): CapabilityDescriptor
    fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies = CapabilityDependencies.empty(),
    ): Capability
}





