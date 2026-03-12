package io.qpointz.mill.ai

/**
 * Generic observation contract.
 *
 * The observer evaluates the latest completed step and decides how the runtime should proceed.
 */
fun interface Observer {
    fun observe(input: ObservationInput): Observation
}

data class ObservationInput(
    val runState: RunState,
    val lastPlannerDecision: PlannerDecision? = null,
    val toolResult: Any? = null,
    val failure: Throwable? = null,
)
