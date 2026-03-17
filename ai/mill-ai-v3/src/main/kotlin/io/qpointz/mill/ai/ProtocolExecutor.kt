package io.qpointz.mill.ai

/**
 * Executes a selected protocol mode against model output.
 *
 * Responsibilities:
 * - execute the selected protocol mode (TEXT, STRUCTURED_FINAL, STRUCTURED_STREAM)
 * - validate outputs against declared schemas
 * - emit protocol events via the listener
 * - return normalized text result to the runtime
 *
 * This abstraction is separate from the planner, tool executor, and capability declaration.
 */
fun interface ProtocolExecutor {
    fun execute(input: ProtocolExecutionInput): ProtocolExecutionResult
}

data class ProtocolExecutionInput(
    val protocol: ProtocolDefinition,
    val runState: RunState,
    val messages: List<Any>,   // opaque to core; adapter provides concrete message types
    val listener: (AgentEvent) -> Unit,
)

data class ProtocolExecutionResult(
    val text: String,
    val payload: String? = null,
)
