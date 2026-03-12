package io.qpointz.mill.ai

/**
 * Generic planning contract.
 *
 * Implementations are expected to be family-specific in policy while sharing this runtime-facing
 * invocation shape.
 */
fun interface Planner {
    fun plan(input: PlannerInput): PlannerDecision
}

data class PlannerInput(
    val runState: RunState,
    val userInput: String,
    val capabilities: List<Capability>,
    val availableTools: List<ToolDefinition>,
)
