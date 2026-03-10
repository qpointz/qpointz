package io.qpointz.mill.ai

/** Describes a single structured input field accepted by a tool. */
data class ToolField(
    val name: String,
    val description: String,
    val required: Boolean = true,
)

/** Runtime request passed to a tool handler. */
data class ToolRequest(
    val arguments: Map<String, String> = emptyMap(),
)

/** Runtime response returned by a tool handler. */
data class ToolResult(
    val content: Map<String, Any?> = emptyMap(),
)

/** Functional adapter so trivial tools can be declared inline. */
fun interface ToolHandler {
    fun invoke(request: ToolRequest): ToolResult
}

/**
 * Minimal tool contract used by the hello-world capabilities.
 *
 * The contract is intentionally narrow: structured string inputs and a serializable output map
 * are enough to validate discovery, tool calling, and later MCP-friendly descriptions.
 */
data class ToolDefinition(
    val name: String,
    val description: String,
    val inputFields: List<ToolField> = emptyList(),
    val outputDescription: String,
    val handler: ToolHandler,
)
