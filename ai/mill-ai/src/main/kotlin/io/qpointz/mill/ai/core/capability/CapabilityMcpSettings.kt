package io.qpointz.mill.ai.core.capability

/**
 * Per-capability MCP exposure settings loaded from the capability YAML manifest.
 *
 * @param enabled When `false`, the entire capability is hidden from MCP catalog and invocation.
 */
data class CapabilityMcpSettings(
    val enabled: Boolean = true,
)

/**
 * Manifest-level tool metadata without an imperative handler.
 *
 * @param name Tool name as declared in YAML.
 * @param description Tool description.
 * @param inputSchema Input JSON schema for MCP registration.
 * @param outputSchema Output JSON schema when declared in YAML.
 * @param kind Tool classification (QUERY or CAPTURE).
 */
data class DeclaredToolMetadata(
    val name: String,
    val description: String,
    val inputSchema: dev.langchain4j.model.chat.request.json.JsonObjectSchema,
    val outputSchema: dev.langchain4j.model.chat.request.json.JsonObjectSchema?,
    val kind: io.qpointz.mill.ai.core.tool.ToolKind,
)
