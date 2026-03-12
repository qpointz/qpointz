package io.qpointz.mill.ai

/**
 * Observer result after a runtime step completes.
 *
 * The observer evaluates what happened and decides how the run should proceed next.
 */
data class Observation(
    val decision: ObservationDecision,
    val reason: String,
    val nextGoal: String? = null,
    val shouldPersist: Boolean = false,
)

enum class ObservationDecision {
    CONTINUE,
    ANSWER,
    CLARIFY,
    STOP_BUDGET,
    FAIL,
}
