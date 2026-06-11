package io.qpointz.mill.ai.core.artifact

import io.qpointz.mill.ai.core.capability.CapabilityManifest

/**
 * Aggregated index of artefact descriptors from capability manifests.
 *
 * Used by the emission coordinator, event router, and chat runtime SSE bridge.
 */
class ArtifactDescriptorRegistry private constructor(
    private val descriptors: List<ArtifactDescriptor>,
    private val byId: Map<String, ArtifactDescriptor>,
    private val byProtocolId: Map<String, ArtifactDescriptor>,
    private val byToolResultArtifactKind: Map<String, ArtifactDescriptor>,
    private val emitTriggersByTool: Map<String, List<ToolEmitTrigger>>,
) {

    /** All registered descriptors. */
    fun all(): List<ArtifactDescriptor> = descriptors

    /** Lookup by descriptor id (capability-local key). */
    fun descriptorById(id: String): ArtifactDescriptor? = byId[id]

    /** Lookup by fully qualified id `capabilityId.descriptorId`. */
    fun descriptorByQualifiedId(qualifiedId: String): ArtifactDescriptor? =
        byId[qualifiedId] ?: run {
            val dot = qualifiedId.indexOf('.')
            if (dot <= 0) return descriptorById(qualifiedId)
            val capabilityId = qualifiedId.substring(0, dot)
            val descriptorId = qualifiedId.substring(dot + 1)
            descriptors.firstOrNull { it.capabilityId == capabilityId && it.id.endsWith(".$descriptorId") }
        }

    /**
     * Resolves a descriptor for [protocolId] routing and SSE projection.
     *
     * @param protocolId Protocol id from [io.qpointz.mill.ai.runtime.events.AgentEvent.ProtocolFinal].
     */
    fun descriptorForProtocol(protocolId: String): ArtifactDescriptor? = byProtocolId[protocolId]

    /**
     * Resolves a descriptor for tool-result `artifactType` values.
     *
     * @param artifactKind Value of `artifactType` in a tool result payload.
     */
    fun descriptorForToolResultArtifactKind(artifactKind: String): ArtifactDescriptor? =
        byToolResultArtifactKind[artifactKind]

    /**
     * Returns emit triggers declared for a tool name across all capabilities.
     *
     * @param toolName Tool handler name.
     */
    fun emitTriggersForTool(toolName: String): List<ToolEmitTrigger> =
        emitTriggersByTool[toolName].orEmpty()

    companion object {
        private val DEFAULT_MANIFEST_RESOURCES = listOf(
            "capabilities/sql-query.yaml",
            "capabilities/metadata-authoring.yaml",
            "capabilities/schema-authoring.yaml",
        )

        /** Loads descriptors from the default POC capability manifests on the classpath. */
        fun loadDefault(): ArtifactDescriptorRegistry =
            fromManifests(DEFAULT_MANIFEST_RESOURCES.map { CapabilityManifest.load(it) })

        /**
         * Builds a registry from loaded [CapabilityManifest] instances.
         *
         * @param manifests Capability manifests that may declare `artifacts:` blocks.
         */
        fun fromManifests(manifests: List<CapabilityManifest>): ArtifactDescriptorRegistry {
            val descriptors = manifests.flatMap { it.artifactDescriptors }
            val triggers = manifests.flatMap { manifest ->
                manifest.toolEmitTriggers.flatMap { (_, toolTriggers) ->
                    toolTriggers.map { trigger ->
                        trigger.copy(artifactId = "${manifest.name}.${trigger.artifactId}")
                    }
                }
            }.groupBy { it.toolName }

            val qualifiedDescriptors = descriptors.map { descriptor ->
                descriptor.copy(id = "${descriptor.capabilityId}.${descriptor.id}")
            }

            val duplicateKeys = qualifiedDescriptors
                .groupBy { it.persistKind to it.sourceEvent }
                .filter { it.value.size > 1 }
            require(duplicateKeys.isEmpty()) {
                "duplicate persistKind+sourceEvent descriptors: ${duplicateKeys.keys}"
            }

            return ArtifactDescriptorRegistry(
                descriptors = qualifiedDescriptors,
                byId = qualifiedDescriptors.associateBy { it.id },
                byProtocolId = qualifiedDescriptors
                    .filter { it.protocolId != null }
                    .associateBy { it.protocolId!! },
                byToolResultArtifactKind = qualifiedDescriptors
                    .filter { it.sourceEvent == ArtifactSourceEvent.TOOL_RESULT }
                    .associateBy { it.artifactKind },
                emitTriggersByTool = triggers,
            )
        }
    }
}
