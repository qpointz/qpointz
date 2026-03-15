package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.ai.*

/**
 * Provider for the schema metadata-authoring capability.
 *
 * No external dependencies are required — the capability produces structured capture
 * artifacts in memory without writing to any metadata store. Persistence is the
 * caller's responsibility after receiving the [AgentEvent.ProtocolFinal] event.
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
 *
 * The [serializedPayload] map is facet-aligned and ready for later persistence by the caller.
 * No metadata storage is touched during the agent turn — this capability is capture-only.
 *
 * @property captureType Subtype discriminator: `"description"` or `"relation"`.
 * @property targetEntityId Canonical fully-qualified entity id (schema.table or schema.table.column).
 * @property targetEntityType Entity type: `"TABLE"` or `"COLUMN"`.
 * @property serializedPayload Facet-aligned key/value payload ready for persistence.
 * @property validationWarnings Warnings accumulated during serialization, empty when clean.
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
 * Contributes two CAPTURE tools ([capture_description], [capture_relation]) and two prompts
 * ([schema-authoring.intent], [schema-authoring.request]). The [schema-authoring.capture]
 * STRUCTURED_FINAL protocol drives final synthesis once a capture tool completes.
 *
 * This capability does not declare grounding tools — it relies on the [SchemaCapability]
 * to supply `list_schemas`, `list_tables`, `list_columns`, and `list_relations` as the
 * grounding layer. Profiles that use this capability should also include [SchemaCapability].
 */
private data class SchemaAuthoringCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/schema-authoring.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    /**
     * Typed args for [capture_description].
     *
     * All fields mirror the `input` schema declared in `schema-authoring.yaml`.
     */
    private data class CaptureDescriptionArgs(
        val targetEntityId: String,
        val targetEntityType: String,
        val description: String,
        val displayName: String? = null,
        val rationale: String? = null,
    )

    /**
     * Typed args for [capture_relation].
     *
     * [sourceColumnIds] and [targetColumnIds] are optional — the planner may omit them
     * when column-level join details are not yet known.
     */
    private data class CaptureRelationArgs(
        val relationName: String,
        val sourceTableId: String,
        val targetTableId: String,
        val sourceColumnIds: List<String> = emptyList(),
        val targetColumnIds: List<String> = emptyList(),
        val description: String? = null,
        val rationale: String? = null,
    )

    /**
     * Typed args for [request_clarification].
     *
     * In normal operation this handler is never reached — the planner in
     * [SchemaExplorationAgent] intercepts the model's `request_clarification` call and converts
     * it to [PlannerDecision.askClarification] before the tool executor runs. This handler
     * exists only as a safety net so the tool specification is registered correctly via the
     * manifest and the toolSpecs list passed to the LLM is complete.
     */
    private data class RequestClarificationArgs(val question: String)

    override val tools: List<ToolDefinition> = listOf(
        manifest.tool("request_clarification") { request ->
            val args = request.argumentsAs<RequestClarificationArgs>()
            ToolResult(mapOf("acknowledged" to true, "question" to args.question))
        },
        manifest.tool("capture_description") { request ->
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
        manifest.tool("capture_relation") { request ->
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
