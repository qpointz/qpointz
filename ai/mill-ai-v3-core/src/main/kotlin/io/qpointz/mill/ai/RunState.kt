package io.qpointz.mill.ai

/**
 * Placeholder runtime state model for the early v3 skeleton.
 *
 * It is intentionally small, but it establishes that workflow state is runtime-owned and can be
 * extended without coupling future agents to chat transcript reconstruction.
 */
data class RunState(
    val profile: AgentProfile,
    val context: AgentContext,
    val artifacts: List<Artifact> = emptyList(),
)
