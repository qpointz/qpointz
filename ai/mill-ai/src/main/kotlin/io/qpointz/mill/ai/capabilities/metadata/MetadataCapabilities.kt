package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.ai.core.capability.Capability
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityDescriptor
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.capability.CapabilityProvider
import io.qpointz.mill.ai.core.prompt.PromptAsset
import io.qpointz.mill.ai.core.protocol.ProtocolDefinition
import io.qpointz.mill.ai.core.tool.ToolBinding
import io.qpointz.mill.ai.core.tool.ToolKind
import io.qpointz.mill.ai.core.tool.ToolRequest
import io.qpointz.mill.ai.core.tool.ToolResult
import io.qpointz.mill.ai.core.tool.argumentsAs
import io.qpointz.mill.ai.runtime.AgentContext

class MetadataCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "metadata",
        name = "Metadata",
        description = "Facet catalog reads and local payload validation",
        supportedContexts = setOf("general"),
        tags = setOf("metadata", "facets"),
        requiredDependencies = setOf(MetadataCapabilityDependency::class.java),
    )

    override fun create(context: AgentContext, dependencies: CapabilityDependencies): Capability =
        MetadataCapability(
            descriptor(),
            dependencies.require(MetadataCapabilityDependency::class.java).port,
        )
}

class MetadataAuthoringCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "metadata-authoring",
        name = "Metadata Authoring",
        description = "Facet assignment proposal capture for metadata entities",
        supportedContexts = setOf("general"),
        tags = setOf("metadata", "facets", "authoring"),
        requiredDependencies = setOf(MetadataCapabilityDependency::class.java),
    )

    override fun create(context: AgentContext, dependencies: CapabilityDependencies): Capability =
        MetadataAuthoringCapability(
            descriptor(),
            dependencies.require(MetadataCapabilityDependency::class.java).port,
        )
}

private data class ListEntityFacetsArgs(
    val metadataEntityId: String,
    val scope: String? = null,
    val context: String? = null,
    val origin: String? = null,
)

private data class ValidateFacetPayloadArgs(
    val facetTypeKey: String,
    val payload: Map<String, Any?>,
)

private data class ProposeFacetAssignmentArgs(
    val facetTypeKey: String,
    val metadataEntityId: String,
    val payload: Map<String, Any?>,
    val rationale: String? = null,
)

data class MetadataFacetProposalCapture(
    val captureType: String = "facet_assignment",
    val facetTypeKey: String,
    val metadataEntityId: String,
    val serializedPayload: Map<String, Any?>,
    val rationale: String?,
    val validationWarnings: List<String> = emptyList(),
)

private data class MetadataCapability(
    override val descriptor: CapabilityDescriptor,
    private val port: MetadataReadPort,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/metadata.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = emptyList()

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("list_facet_types") { ToolResult(port.listFacetTypes()) },
        manifest.tool("list_entity_facets") { request ->
            val args = request.argumentsAs<ListEntityFacetsArgs>()
            ToolResult(port.listEntityFacets(args.metadataEntityId, args.scope, args.context, args.origin))
        },
        manifest.tool("validate_facet_payload") { request ->
            val args = request.argumentsAs<ValidateFacetPayloadArgs>()
            val errs = validateFacetPayloadInternal(port, args.facetTypeKey, args.payload)
            ToolResult(
                mapOf(
                    "valid" to errs.isEmpty(),
                    "errors" to errs,
                ),
            )
        },
    )
}

private data class MetadataAuthoringCapability(
    override val descriptor: CapabilityDescriptor,
    private val port: MetadataReadPort,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/metadata-authoring.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("propose_facet_assignment", kindOverride = ToolKind.CAPTURE) { request ->
            val args = request.argumentsAs<ProposeFacetAssignmentArgs>()
            val warnings = mutableListOf<String>()
            val payloadErrors = validateFacetPayloadInternal(port, args.facetTypeKey, args.payload)
            if (payloadErrors.isNotEmpty()) {
                ToolResult(
                    mapOf(
                        "rejected" to true,
                        "errors" to payloadErrors,
                    ),
                )
            } else {
                ToolResult(
                    MetadataFacetProposalCapture(
                        facetTypeKey = args.facetTypeKey,
                        metadataEntityId = args.metadataEntityId,
                        serializedPayload = args.payload,
                        rationale = args.rationale,
                        validationWarnings = warnings,
                    ),
                )
            }
        },
    )
}

internal fun validateFacetPayloadInternal(port: MetadataReadPort, facetTypeKey: String, payload: Map<String, Any?>): List<String> {
    val manifests = port.listFacetTypes()
    val manifest = FacetPayloadStructureValidator.manifestForFacetType(manifests, facetTypeKey)
        ?: return listOf("unknown facet type: $facetTypeKey")
    return FacetPayloadStructureValidator.validate(manifest.payload, payload)
}



