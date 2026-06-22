package io.qpointz.mill.ai.mcp

import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.core.capability.DeclaredToolMetadata
import io.qpointz.mill.ai.core.tool.ToolKind
import io.qpointz.mill.ai.profile.AgentProfile

/**
 * Exposed MCP tool entry shared by catalog listing and executor resolution.
 */
data class McpExposedTool(
    val capabilityId: String,
    val toolName: String,
    val namespacedName: String,
    val description: String,
    val inputSchema: JsonObjectSchema,
    val kind: ToolKind,
    val declared: DeclaredToolMetadata,
)

/**
 * Shared exposure index for catalog listing and executor invocation.
 */
internal class McpExposureIndex private constructor(
    val exposedTools: Map<String, McpExposedTool>,
    val resourceDescriptors: List<ExternalCapabilityAssetDescriptor>,
) {
    companion object {
        fun build(
            registry: CapabilityRegistry,
            exposureConfig: McpExposureConfig,
            profile: AgentProfile?,
            manifestLoader: (String) -> CapabilityManifest = CapabilityManifest::load,
        ): McpExposureIndex {
            val allowlist = exposureConfig.capabilities.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            val profileIds = profile?.capabilityIds

            val descriptors = mutableListOf<ExternalCapabilityAssetDescriptor>()
            val tools = linkedMapOf<String, McpExposedTool>()

            registry.allDescriptors().forEach { descriptor ->
                if (profileIds != null && descriptor.id !in profileIds) {
                    return@forEach
                }
                if (allowlist.isNotEmpty() && descriptor.id !in allowlist) {
                    return@forEach
                }

                val manifest = manifestLoader(CapabilityManifest.manifestResourceFor(descriptor.id))
                if (!manifest.mcpSettings.enabled) {
                    return@forEach
                }

                descriptors += ExternalCapabilityAssetDescriptor.Capability(
                    capabilityId = descriptor.id,
                    description = descriptor.description,
                    tags = descriptor.tags,
                )

                manifest.declaredTools().forEach { declared ->
                    val namespaced = namespacedToolName(descriptor.id, declared.name)
                    tools[namespaced] = McpExposedTool(
                        capabilityId = descriptor.id,
                        toolName = declared.name,
                        namespacedName = namespaced,
                        description = declared.description,
                        inputSchema = declared.inputSchema,
                        kind = declared.kind,
                        declared = declared,
                    )
                    descriptors += ExternalCapabilityAssetDescriptor.Tool(
                        capabilityId = descriptor.id,
                        toolName = declared.name,
                        namespacedName = namespaced,
                        toolKind = declared.kind,
                        inputSchema = declared.inputSchema,
                        outputSchema = declared.outputSchema,
                        description = declared.description,
                        tags = descriptor.tags,
                    )
                }

                manifest.allPrompts.forEach { prompt ->
                    descriptors += ExternalCapabilityAssetDescriptor.Prompt(
                        capabilityId = descriptor.id,
                        promptId = prompt.id,
                        description = prompt.description,
                        tags = descriptor.tags,
                    )
                }

                manifest.allProtocols.forEach { protocol ->
                    descriptors += ExternalCapabilityAssetDescriptor.Protocol(
                        capabilityId = descriptor.id,
                        protocolId = protocol.id,
                        mode = protocol.mode.name,
                        description = protocol.description,
                        tags = descriptor.tags,
                    )
                }

                manifest.artifactDescriptors.forEach { artifact ->
                    descriptors += ExternalCapabilityAssetDescriptor.ArtifactSchema(
                        capabilityId = descriptor.id,
                        artifactKind = artifact.artifactKind,
                        description = artifact.artifactKind,
                        tags = descriptor.tags,
                    )
                }
            }

            return McpExposureIndex(
                exposedTools = tools,
                resourceDescriptors = descriptors,
            )
        }

        private fun namespacedToolName(capabilityId: String, toolName: String): String =
            "$capabilityId.$toolName"
    }
}
