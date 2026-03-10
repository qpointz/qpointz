package io.qpointz.mill.ai

/**
 * Minimal observer result for the hello-world milestone.
 *
 * The observer remains application-owned even when the model is used for planning and answer
 * generation.
 */
data class Observation(
    val decision: ObservationDecision,
    val reason: String,
)

enum class ObservationDecision {
    CONTINUE,
    FINISH,
    FAIL,
}
