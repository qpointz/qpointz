package io.qpointz.mill.ai.core.protocol

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

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





