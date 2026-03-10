package io.qpointz.mill.ai

/**
 * Minimal profile model for binding a runtime to a capability set.
 *
 * The current hello-world runtime uses a single fixed profile, but the same shape will support
 * future context-specific agents assembled from different capabilities.
 */
data class AgentProfile(
    val id: String,
    val capabilityIds: Set<String>,
)
