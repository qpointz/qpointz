package io.qpointz.mill.ai.mcp

/**
 * Server-side MCP exposure configuration applied after per-capability `mcp.enabled`.
 *
 * @param capabilities When non-empty, only listed capability ids are exposed.
 */
data class McpExposureConfig(
    val capabilities: List<String> = emptyList(),
)

/**
 * Admission decision for MCP tool invocation (POC stub; align with backlog A-79 later).
 */
enum class McpAdmissionDecision {
    ALLOW,
    DENY,
}

/**
 * Authorizes MCP tool invocations after catalog exposure filters.
 */
fun interface CapabilityAdmissionGate {
    /**
     * @param capabilityId Capability id.
     * @param toolName Manifest tool name (without namespace prefix).
     * @return ALLOW to proceed or DENY to reject.
     */
    fun authorizeTool(capabilityId: String, toolName: String): McpAdmissionDecision
}

/** Default POC gate that allows all exposed tools. */
class PermissiveAdmissionGate : CapabilityAdmissionGate {
    override fun authorizeTool(capabilityId: String, toolName: String): McpAdmissionDecision =
        McpAdmissionDecision.ALLOW
}
