package io.qpointz.mill.ai

/** Runtime request passed to a tool handler. */
data class ToolRequest(
    val arguments: Map<String, Any?> = emptyMap(),
    val context: ToolExecutionContext = ToolExecutionContext(),
)

/** Runtime context passed to a single tool invocation. */
data class ToolExecutionContext(
    val agentContext: AgentContext? = null,
    private val attributes: Map<Class<*>, Any> = emptyMap(),
) {
    fun <T : Any> get(type: Class<T>): T? = type.cast(attributes[type])

    fun <T : Any> require(type: Class<T>): T =
        requireNotNull(get(type)) { "Missing tool execution attribute: ${type.name}" }

    companion object {
        fun of(agentContext: AgentContext? = null, vararg attributes: Any): ToolExecutionContext =
            ToolExecutionContext(
                agentContext = agentContext,
                attributes = attributes.associateBy { it.javaClass },
            )
    }
}

/** Runtime response returned by a tool handler. */
data class ToolResult(
    val content: Any? = null,
)

/** Functional adapter so trivial tools can be declared inline. */
fun interface ToolHandler {
    fun invoke(request: ToolRequest): ToolResult
}

/**
 * Classifies a tool by its runtime role.
 *
 * - [QUERY]   — read-only; the result informs the next planning step (default).
 * - [CAPTURE] — side-effecting; the tool produces a terminal artifact. The agent
 *               should treat a completed CAPTURE call as ready for synthesis.
 */
enum class ToolKind { QUERY, CAPTURE }
