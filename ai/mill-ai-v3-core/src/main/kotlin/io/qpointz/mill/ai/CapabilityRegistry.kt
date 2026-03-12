package io.qpointz.mill.ai

import java.util.ServiceLoader

/**
 * Runtime registry over discovered capability providers.
 *
 * The initial implementation intentionally keeps discovery simple and framework-free.
 */
class CapabilityRegistry private constructor(
    private val providers: Map<String, CapabilityProvider>,
) {

    /** Return descriptors without instantiating capabilities. */
    fun allDescriptors(): List<CapabilityDescriptor> =
        providers.values
            .map { it.descriptor() }
            .sortedBy { it.id }

    /** Filter providers by the coarse runtime context. */
    fun providersFor(contextType: String): List<CapabilityProvider> =
        providers.values
            .filter { provider ->
                val contexts = provider.descriptor().supportedContexts
                contexts.isEmpty() || contextType in contexts
            }
            .sortedBy { it.descriptor().id }

    /** Materialize capability instances for a concrete run context. */
    fun capabilitiesFor(context: AgentContext): List<Capability> =
        providersFor(context.contextType)
            .map { provider ->
                val descriptor = provider.descriptor()
                val dependencies = context.capabilityDependencies.forCapability(descriptor.id)
                validateDependencies(descriptor, dependencies)
                provider.create(context, dependencies)
            }

    /** Materialize the exact capability set declared by a profile for a concrete run context. */
    fun capabilitiesFor(profile: AgentProfile, context: AgentContext): List<Capability> {
        val providersById = providersFor(context.contextType).associateBy { it.descriptor().id }
        val missing = profile.capabilityIds.filter { it !in providersById }
        require(missing.isEmpty()) {
            "Missing providers for profile `${profile.id}`: $missing"
        }

        return profile.capabilityIds
            .sorted()
            .map { capabilityId ->
                val provider = providersById.getValue(capabilityId)
                val descriptor = provider.descriptor()
                val dependencies = context.capabilityDependencies.forCapability(capabilityId)
                validateDependencies(descriptor, dependencies)
                provider.create(context, dependencies)
            }
    }

    /** Lookup a provider by capability id. */
    fun provider(id: String): CapabilityProvider? = providers[id]

    private fun validateDependencies(
        descriptor: CapabilityDescriptor,
        dependencies: CapabilityDependencies,
    ) {
        val missing = descriptor.requiredDependencies.filterNot(dependencies::contains)
        require(missing.isEmpty()) {
            "Missing dependencies for capability `${descriptor.id}`: ${missing.map(Class<*>::getName)}"
        }
    }

    companion object {
        /** Build a registry from an explicit provider list, mainly for tests or adapters. */
        fun from(providers: Iterable<CapabilityProvider>): CapabilityRegistry =
            CapabilityRegistry(providers.associateBy { it.descriptor().id })

        /** Load providers from `META-INF/services` using the current class loader. */
        fun load(classLoader: ClassLoader = Thread.currentThread().contextClassLoader): CapabilityRegistry {
            val loaded = ServiceLoader.load(CapabilityProvider::class.java, classLoader)
                .iterator()
                .asSequence()
                .toList()
            return from(loaded)
        }
    }
}
