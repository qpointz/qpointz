package io.qpointz.mill.ai

/**
 * Minimal structured planning decision for the hello-world milestone.
 *
 * This keeps the first agentic loop explicit without introducing a heavyweight planning model
 * before the workflow-validation milestones.
 */
data class PlannerDecision(
    val mode: Mode,
    val toolName: String? = null,
    val toolArguments: Map<String, String> = emptyMap(),
    val rationale: String? = null,
) {
    enum class Mode {
        DIRECT_RESPONSE,
        CALL_TOOL,
    }
}
