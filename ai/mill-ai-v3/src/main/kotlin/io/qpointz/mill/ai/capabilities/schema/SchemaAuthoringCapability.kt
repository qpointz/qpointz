package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.ai.*

/**
 * Provider for the schema metadata-authoring capability.
 */
class SchemaAuthoringCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "schema-authoring",
        name = "Schema Authoring",
        description = "Metadata authoring capability: produces DescriptiveFacet and RelationFacet capture artifacts",
        supportedContexts = setOf("general"),
        tags = setOf("schema", "authoring"),
        requiredDependencies = emptySet(),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = SchemaAuthoringCapability(descriptor())
}

/**
 * Canonical capture result returned by both [capture_description] and [capture_relation] tools.
 */
data class CaptureResult(
    val captureType: String,
    val targetEntityId: String,
    val targetEntityType: String,
    val serializedPayload: Map<String, Any?>,
    val validationWarnings: List<String> = emptyList(),
)

/**
 * Schema metadata-authoring capability.
 *
 * Contributes two CAPTURE tools and a STRUCTURED_FINAL protocol for synthesis.
 */
private data class SchemaAuthoringCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/schema-authoring.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    private data class CaptureDescriptionArgs(
        val targetEntityId: String,
        val targetEntityType: String,
        val description: String,
        val displayName: String? = null,
        val rationale: String? = null,
    )

    private data class CaptureRelationArgs(
        val relationName: String,
        val sourceTableId: String,
        val targetTableId: String,
        val sourceColumnIds: List<String> = emptyList(),
        val targetColumnIds: List<String> = emptyList(),
        val description: String? = null,
        val rationale: String? = null,
    )

    private data class RequestClarificationArgs(val question: String)

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("request_clarification") { request ->
            val args = request.argumentsAs<RequestClarificationArgs>()
            ToolResult(mapOf("acknowledged" to true, "question" to args.question))
        },
        manifest.tool("capture_description", kindOverride = ToolKind.CAPTURE) { request ->
            val args = request.argumentsAs<CaptureDescriptionArgs>()
            val warnings = mutableListOf<String>()
            if (args.description.isBlank()) {
                warnings += "description is blank — the capture may produce an empty metadata entry"
            }
            val payload = buildMap<String, Any?> {
                put("facetType", "descriptive")
                put("description", args.description)
                args.displayName?.let { put("displayName", it) }
                args.rationale?.let { put("rationale", it) }
            }
            ToolResult(
                CaptureResult(
                    captureType = "description",
                    targetEntityId = args.targetEntityId,
                    targetEntityType = args.targetEntityType,
                    serializedPayload = payload,
                    validationWarnings = warnings,
                )
            )
        },
        manifest.tool("capture_relation", kindOverride = ToolKind.CAPTURE) { request ->
            val args = request.argumentsAs<CaptureRelationArgs>()
            val warnings = mutableListOf<String>()
            if (args.sourceColumnIds.isEmpty() && args.targetColumnIds.isEmpty()) {
                warnings += "neither sourceColumnIds nor targetColumnIds were supplied — relation will be table-level only"
            }
            val relation = buildMap<String, Any?> {
                put("name", args.relationName)
                put("sourceTable", args.sourceTableId)
                put("targetTable", args.targetTableId)
                put("sourceColumns", args.sourceColumnIds)
                put("targetColumns", args.targetColumnIds)
                args.description?.let { put("description", it) }
            }
            val payload = buildMap<String, Any?> {
                put("facetType", "relation")
                put("relations", listOf(relation))
                args.rationale?.let { put("rationale", it) }
            }
            ToolResult(
                CaptureResult(
                    captureType = "relation",
                    targetEntityId = args.sourceTableId,
                    targetEntityType = "TABLE",
                    serializedPayload = payload,
                    validationWarnings = warnings,
                )
            )
        },
    )
}
