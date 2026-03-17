package io.qpointz.mill.ai

import dev.langchain4j.agent.tool.ToolSpecification

/**
 * Binds a LangChain4j [ToolSpecification] to a [ToolHandler] and classifies the tool by [ToolKind].
 *
 * Replaces the old framework-free `ToolDefinition` / `ToolSchema` type hierarchy.
 * The [spec] is built directly using LangChain4j's JSON-schema builder, eliminating the
 * custom schema model and the separate `ToolSchemaConverter` translation step.
 */
data class ToolBinding(
    val spec: ToolSpecification,
    val handler: ToolHandler,
    val kind: ToolKind = ToolKind.QUERY,
)
