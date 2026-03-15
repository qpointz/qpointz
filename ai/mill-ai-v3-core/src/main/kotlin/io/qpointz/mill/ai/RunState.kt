package io.qpointz.mill.ai

/**
 * Runtime-owned state for a single agent run.
 *
 * This state is intentionally framework-free and small enough for early agents, while being rich
 * enough to support explicit planner/observer handoffs and future durable persistence.
 */
data class RunState(
    val profile: AgentProfile,
    val context: AgentContext,
    val runId: String? = null,
    val conversationId: String? = null,
    val steps: List<RunStep> = emptyList(),
    val artifacts: List<Artifact> = emptyList(),
    val clarificationCount: Int = 0,
    val toolCallCount: Int = 0,
) {
    val iteration: Int
        get() = steps.size

    fun appendStep(step: RunStep): RunState = copy(
        steps = steps + step,
        clarificationCount = clarificationCount + if (step.kind == RunStepKind.CLARIFICATION) 1 else 0,
        toolCallCount = toolCallCount + if (step.kind == RunStepKind.TOOL_CALL) 1 else 0,
    )

    fun addArtifact(artifact: Artifact): RunState = copy(artifacts = artifacts + artifact)
}

data class RunStep(
    val kind: RunStepKind,
    val plannerDecision: PlannerDecision? = null,
    val observation: Observation? = null,
    val toolName: String? = null,
    val toolResult: String? = null,
    val summary: String? = null,
    val selectedProtocolId: String? = null,
    val protocolPayload: String? = null,
)

enum class RunStepKind {
    PLAN,
    TOOL_CALL,
    SYNTHESIS,
    CLARIFICATION,
    FAILURE,
}
