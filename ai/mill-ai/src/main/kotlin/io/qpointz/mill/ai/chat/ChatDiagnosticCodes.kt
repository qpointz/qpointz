package io.qpointz.mill.ai.chat

/**
 * Stable [ChatRuntimeEvent.Diagnostic.code] values for clients that switch on strings in the UX layer.
 *
 * Runtimes may add new codes without breaking clients that treat unknown codes as generic status text.
 */
object ChatDiagnosticCodes {
    /** Agent run began for the current profile. */
    const val RUN_STARTED = "run.started"

    /** High-level planner / next-step hint (when the runtime emits it). */
    const val PLAN_CREATED = "plan.created"

    /** User-visible thinking / progress line from the agent (not model token stream). */
    const val THINKING_DELTA = "thinking.delta"

    /** Extended-thinking or reasoning token stream from the model (when supported). */
    const val REASONING_DELTA = "reasoning.delta"
}
