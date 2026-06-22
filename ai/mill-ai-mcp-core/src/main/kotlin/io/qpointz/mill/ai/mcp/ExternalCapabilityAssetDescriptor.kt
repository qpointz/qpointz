package io.qpointz.mill.ai.mcp

import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import io.qpointz.mill.ai.core.tool.ToolKind

/**
 * Self-describing metadata for externally exposed capability assets (MCP resources).
 *
 * Clients can identify resource kind, owning capability, and interpretation hints without
 * reading opaque blob content first.
 */
sealed interface ExternalCapabilityAssetDescriptor {
    /** Owning capability id. */
    val capabilityId: String

    /** Descriptor variant kind (for example `tool`, `prompt`). */
    val assetKind: String

    /** Descriptor schema version. */
    val version: String

    /** Canonical `mill://` URI for this asset. */
    val uri: String

    /** MIME type for serialized descriptor payloads. */
    val contentType: String

    /** Human-readable summary. */
    val description: String

    /** Optional classification tags. */
    val tags: Set<String>

    /** Placeholder for future trust/RBAC alignment (backlog A-79). */
    val trustClass: String?

    /**
     * Capability-level descriptor resource (`mill://capabilities/{id}`).
     *
     * @param capabilityId Capability id.
     * @param version Descriptor schema version.
     * @param uri Canonical URI.
     * @param contentType MIME type.
     * @param description Capability summary.
     * @param tags Classification tags.
     * @param trustClass Optional trust class placeholder.
     */
    data class Capability(
        override val capabilityId: String,
        override val version: String = DEFAULT_VERSION,
        override val uri: String = McpUriScheme.capability(capabilityId),
        override val contentType: String = JSON_CONTENT_TYPE,
        override val description: String,
        override val tags: Set<String> = emptySet(),
        override val trustClass: String? = null,
    ) : ExternalCapabilityAssetDescriptor {
        override val assetKind: String = ASSET_KIND_CAPABILITY
    }

    /**
     * Tool contract metadata including input and optional output JSON schemas.
     *
     * @param capabilityId Owning capability id.
     * @param toolName Manifest tool name (without namespace prefix).
     * @param namespacedName MCP tool name (`{capabilityId}.{toolName}`).
     * @param toolKind QUERY or CAPTURE (metadata only in POC).
     * @param inputSchema Tool input JSON schema from manifest.
     * @param outputSchema Tool output JSON schema when declared in manifest.
     * @param version Descriptor schema version.
     * @param uri Canonical URI.
     * @param contentType MIME type.
     * @param description Tool summary.
     * @param tags Classification tags.
     * @param trustClass Optional trust class placeholder.
     */
    data class Tool(
        override val capabilityId: String,
        val toolName: String,
        val namespacedName: String,
        val toolKind: ToolKind,
        val inputSchema: JsonObjectSchema,
        val outputSchema: JsonObjectSchema?,
        override val version: String = DEFAULT_VERSION,
        override val uri: String = McpUriScheme.tool(capabilityId, toolName),
        override val contentType: String = JSON_CONTENT_TYPE,
        override val description: String,
        override val tags: Set<String> = emptySet(),
        override val trustClass: String? = null,
    ) : ExternalCapabilityAssetDescriptor {
        override val assetKind: String = ASSET_KIND_TOOL
    }

    /**
     * Protocol schema / mode metadata.
     *
     * @param capabilityId Owning capability id.
     * @param protocolId Protocol id from manifest.
     * @param mode Protocol mode name.
     * @param version Descriptor schema version.
     * @param uri Canonical URI.
     * @param contentType MIME type.
     * @param description Protocol summary.
     * @param tags Classification tags.
     * @param trustClass Optional trust class placeholder.
     */
    data class Protocol(
        override val capabilityId: String,
        val protocolId: String,
        val mode: String,
        override val version: String = DEFAULT_VERSION,
        override val uri: String = McpUriScheme.protocol(capabilityId, protocolId),
        override val contentType: String = JSON_CONTENT_TYPE,
        override val description: String,
        override val tags: Set<String> = emptySet(),
        override val trustClass: String? = null,
    ) : ExternalCapabilityAssetDescriptor {
        override val assetKind: String = ASSET_KIND_PROTOCOL
    }

    /**
     * Prompt asset metadata.
     *
     * @param capabilityId Owning capability id.
     * @param promptId Prompt id from manifest.
     * @param version Descriptor schema version.
     * @param uri Canonical URI.
     * @param contentType MIME type.
     * @param description Prompt summary.
     * @param tags Classification tags.
     * @param trustClass Optional trust class placeholder.
     */
    data class Prompt(
        override val capabilityId: String,
        val promptId: String,
        override val version: String = DEFAULT_VERSION,
        override val uri: String = McpUriScheme.prompt(capabilityId, promptId),
        override val contentType: String = JSON_CONTENT_TYPE,
        override val description: String,
        override val tags: Set<String> = emptySet(),
        override val trustClass: String? = null,
    ) : ExternalCapabilityAssetDescriptor {
        override val assetKind: String = ASSET_KIND_PROMPT
    }

    /**
     * Artifact kind schema reference (`mill://artifacts/{kind}`).
     *
     * @param capabilityId Owning capability id when artifact is capability-scoped.
     * @param artifactKind Artifact kind identifier.
     * @param version Descriptor schema version.
     * @param uri Canonical URI.
     * @param contentType MIME type.
     * @param description Artifact schema summary.
     * @param tags Classification tags.
     * @param trustClass Optional trust class placeholder.
     */
    data class ArtifactSchema(
        override val capabilityId: String,
        val artifactKind: String,
        override val version: String = DEFAULT_VERSION,
        override val uri: String = McpUriScheme.artifactSchema(artifactKind),
        override val contentType: String = JSON_CONTENT_TYPE,
        override val description: String,
        override val tags: Set<String> = emptySet(),
        override val trustClass: String? = null,
    ) : ExternalCapabilityAssetDescriptor {
        override val assetKind: String = ASSET_KIND_ARTIFACT_SCHEMA
    }

    companion object {
        const val DEFAULT_VERSION: String = "1.0"
        const val JSON_CONTENT_TYPE: String = "application/json"

        const val ASSET_KIND_CAPABILITY: String = "capability"
        const val ASSET_KIND_TOOL: String = "tool"
        const val ASSET_KIND_PROTOCOL: String = "protocol"
        const val ASSET_KIND_PROMPT: String = "prompt"
        const val ASSET_KIND_ARTIFACT_SCHEMA: String = "artifact-schema"
    }
}
