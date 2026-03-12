package io.qpointz.mill.ai

/**
 * Structured runtime planning decision.
 *
 * The decision model is generic at the runtime layer. Individual agent families are expected to
 * use only the subset of actions that make sense for their workflow.
 */
data class PlannerDecision(
    val action: Action,
    val toolName: String? = null,
    val toolArguments: Map<String, Any?> = emptyMap(),
    val toolCalls: List<PlannedToolCall> = emptyList(),
    val rationale: String? = null,
    val clarificationQuestion: String? = null,
    val target: String? = null,
) {
    init {
        require(action == Action.CALL_TOOL || toolName == null) {
            "toolName is only valid for CALL_TOOL decisions."
        }
        require(action == Action.CALL_TOOL || toolArguments.isEmpty()) {
            "toolArguments are only valid for CALL_TOOL decisions."
        }
        require(action == Action.CALL_TOOL || toolCalls.isEmpty()) {
            "toolCalls are only valid for CALL_TOOL decisions."
        }
        require(action == Action.ASK_CLARIFICATION || clarificationQuestion == null) {
            "clarificationQuestion is only valid for ASK_CLARIFICATION decisions."
        }
        require(action != Action.CALL_TOOL || toolName != null || toolCalls.isNotEmpty()) {
            "CALL_TOOL decisions must declare toolName or toolCalls."
        }
    }

    enum class Action {
        DIRECT_RESPONSE,
        CALL_TOOL,
        ASK_CLARIFICATION,
        SYNTHESIZE_ANSWER,
        REPLAN,
        FAIL,
    }

    companion object {
        fun directResponse(rationale: String? = null): PlannerDecision =
            PlannerDecision(action = Action.DIRECT_RESPONSE, rationale = rationale)

        fun callTool(
            toolName: String,
            toolArguments: Map<String, Any?> = emptyMap(),
            rationale: String? = null,
        ): PlannerDecision =
            PlannerDecision(
                action = Action.CALL_TOOL,
                toolName = toolName,
                toolArguments = toolArguments,
                toolCalls = listOf(PlannedToolCall(name = toolName, arguments = toolArguments)),
                rationale = rationale,
            )

        fun callTools(
            toolCalls: List<PlannedToolCall>,
            rationale: String? = null,
        ): PlannerDecision =
            PlannerDecision(
                action = Action.CALL_TOOL,
                toolName = toolCalls.firstOrNull()?.name,
                toolArguments = toolCalls.firstOrNull()?.arguments ?: emptyMap(),
                toolCalls = toolCalls,
                rationale = rationale,
            )

        fun askClarification(
            question: String,
            rationale: String? = null,
        ): PlannerDecision =
            PlannerDecision(
                action = Action.ASK_CLARIFICATION,
                clarificationQuestion = question,
                rationale = rationale,
            )
    }
}

data class PlannedToolCall(
    val requestId: String? = null,
    val name: String,
    val arguments: Map<String, Any?> = emptyMap(),
)
