package io.qpointz.mill.ai

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
