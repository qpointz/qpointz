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
            .map { it.create(context) }

    /** Lookup a provider by capability id. */
    fun provider(id: String): CapabilityProvider? = providers[id]

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
