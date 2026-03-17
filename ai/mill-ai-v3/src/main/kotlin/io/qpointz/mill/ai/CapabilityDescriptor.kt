package io.qpointz.mill.ai

/**
 * Self-describing metadata used for discovery, composition, and future MCP exposure.
 */
data class CapabilityDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val supportedContexts: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val requiredDependencies: Set<Class<out CapabilityDependency>> = emptySet(),
)
