package io.qpointz.mill.ai.mcp

import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.core.tool.ToolRequest
import io.qpointz.mill.ai.runtime.AgentContext

/**
 * Result of an MCP tool invocation.
 *
 * @param content Structured JSON-serializable payload.
 */
data class McpToolCallResult(
    val content: Any?,
)

/**
 * Invokes exposed MCP tools using catalog-scoped resolution and capability handlers.
 *
 * @param registry Capability provider registry.
 * @param catalog Matching catalog built with the same exposure configuration.
 * @param context Agent context for capability materialization.
 * @param admissionGate Admission gate applied before handler invocation.
 */
class CapabilityMcpExecutor(
    private val registry: CapabilityRegistry,
    private val catalog: CapabilityMcpCatalog,
    private val context: AgentContext,
    private val admissionGate: CapabilityAdmissionGate = PermissiveAdmissionGate(),
) {
    /**
     * Invokes a namespaced MCP tool when it is present in the catalog exposure set.
     *
     * @param namespacedToolName MCP tool name (`{capabilityId}.{toolName}`).
     * @param arguments Tool arguments map.
     * @return Structured tool result.
     * @throws McpToolInvocationException when the tool is unknown, filtered, or denied.
     */
    fun callTool(namespacedToolName: String, arguments: Map<String, Any?> = emptyMap()): McpToolCallResult {
        val exposed = catalog.exposedTools[namespacedToolName]
            ?: throw McpToolInvocationException(
                "Tool '$namespacedToolName' is not in the current MCP exposure set",
            )

        if (admissionGate.authorizeTool(exposed.capabilityId, exposed.toolName) == McpAdmissionDecision.DENY) {
            throw McpToolInvocationException(
                "Tool '$namespacedToolName' denied by admission gate",
            )
        }

        val provider = registry.provider(exposed.capabilityId)
            ?: throw McpToolInvocationException(
                "No provider registered for capability '${exposed.capabilityId}'",
            )

        val dependencies = context.capabilityDependencies.forCapability(exposed.capabilityId)
        val capability = provider.create(context, dependencies)
        val binding = capability.tools.firstOrNull { it.spec.name() == exposed.toolName }
            ?: throw McpToolInvocationException(
                "Tool '${exposed.toolName}' not bound in capability '${exposed.capabilityId}'",
            )

        val result = binding.handler.invoke(ToolRequest(arguments = arguments))
        return McpToolCallResult(content = result.content)
    }
}

/**
 * Raised when an MCP tool cannot be invoked under the current exposure configuration.
 *
 * @param message Human-readable rejection reason.
 */
class McpToolInvocationException(message: String) : IllegalArgumentException(message)
