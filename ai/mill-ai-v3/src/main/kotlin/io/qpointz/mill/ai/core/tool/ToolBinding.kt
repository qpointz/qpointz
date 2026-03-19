package io.qpointz.mill.ai.core.tool

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
    /** Protocol id to invoke when this capture tool fires. Null means terminate without synthesis. */
    val protocolId: String? = null,
)





