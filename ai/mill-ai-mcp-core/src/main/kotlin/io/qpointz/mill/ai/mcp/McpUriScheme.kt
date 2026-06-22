package io.qpointz.mill.ai.mcp

/**
 * Constants and builders for Mill MCP resource URIs (`mill://…`).
 */
object McpUriScheme {
    const val SCHEME: String = "mill"

    /** URI for a capability-level descriptor resource. */
    fun capability(capabilityId: String): String = "$SCHEME://capabilities/$capabilityId"

    /** URI for a tool contract resource under a capability. */
    fun tool(capabilityId: String, toolName: String): String =
        "$SCHEME://capabilities/$capabilityId/tools/$toolName"

    /** URI for a protocol schema resource. */
    fun protocol(capabilityId: String, protocolId: String): String =
        "$SCHEME://capabilities/$capabilityId/protocols/$protocolId"

    /** URI for a prompt asset resource. */
    fun prompt(capabilityId: String, promptId: String): String =
        "$SCHEME://capabilities/$capabilityId/prompts/$promptId"

    /** URI for a global artifact kind schema resource. */
    fun artifactSchema(kind: String): String = "$SCHEME://artifacts/$kind"
}
