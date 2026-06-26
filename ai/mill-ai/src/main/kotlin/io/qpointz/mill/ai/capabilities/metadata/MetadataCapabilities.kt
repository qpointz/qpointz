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
import io.qpointz.mill.ai.core.tool.ToolResult
import io.qpointz.mill.ai.core.tool.argumentsAs
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

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
      context,
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
      context,
      dependencies.require(MetadataCapabilityDependency::class.java).port,
    )
}

private data class ListFacetTypesArgs(
  val applicableTo: String? = null,
  val category: String? = null,
  val metadataEntityId: String? = null,
)

private data class GetFacetTypeArgs(
  val facetTypeKey: String,
)

private data class ListContentArgs(
  val targetUrn: String? = null,
  val contentKind: String? = null,
)

private data class GetContentArgs(
  val contentUrn: String,
)

private data class ListEntityFacetsArgs(
  val metadataEntityId: String,
  val scope: String? = null,
  val context: String? = null,
  val origin: String? = null,
)

private data class ValidateFacetPayloadArgs(
  val facetTypeKey: String,
  val payload: Map<String, Any?>,
  val metadataEntityId: String? = null,
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
  val writeScopeUrns: List<String> = emptyList(),
  val validationWarnings: List<String> = emptyList(),
)

private data class MetadataCapability(
  override val descriptor: CapabilityDescriptor,
  private val agentContext: AgentContext,
  private val port: MetadataReadPort,
) : Capability {

  private val manifest = CapabilityManifest.load("capabilities/metadata.yaml")

  override val prompts: List<PromptAsset> = manifest.allPrompts

  override val protocols: List<ProtocolDefinition> = emptyList()

  override val tools: List<ToolBinding> = listOf(
    manifest.tool("list_facet_categories") {
      ToolResult(port.listFacetCategories())
    },
    manifest.tool("list_facet_types") { request ->
      val args = request.argumentsAs<ListFacetTypesArgs>()
      val entityTypeUrn = args.metadataEntityId?.let(MetadataFacetValidation::applicableEntityTypeUrn)
      val rows = port.listFacetTypes()
        .asSequence()
        .filter { args.category.isNullOrBlank() || it.category.equals(args.category, ignoreCase = true) }
        .filter { args.applicableTo.isNullOrBlank() || matchesApplicableTo(it, args.applicableTo!!) }
        .filter { entityTypeUrn == null || matchesApplicableTo(it, entityTypeUrn) }
        .map { it.toSummaryMap() }
        .toList()
      ToolResult(rows)
    },
    manifest.tool("get_facet_type") { request ->
      val args = request.argumentsAs<GetFacetTypeArgs>()
      val key = MetadataUrns.normaliseFacetTypePath(args.facetTypeKey)
      val manifestRow = port.getFacetType(key)
        ?: return@tool ToolResult(mapOf("error" to "unknown facet type: ${args.facetTypeKey}"))
      val typeUrn = MetadataEntityUrn.canonicalize(key)
      val examples = port.listContent(typeUrn, MetadataContent.KIND_FACET_TYPE_EXAMPLE)
        .mapNotNull { row -> row.content as? Map<String, Any?> }
      ToolResult(
        buildMap {
          putAll(manifestRow.toFullMap())
          if (examples.isNotEmpty()) {
            put("examples", examples)
          }
        },
      )
    },
    manifest.tool("list_content") { request ->
      val args = request.argumentsAs<ListContentArgs>()
      ToolResult(port.listContent(args.targetUrn, args.contentKind))
    },
    manifest.tool("get_content") { request ->
      val args = request.argumentsAs<GetContentArgs>()
      val row = port.getContent(args.contentUrn)
      ToolResult(row ?: mapOf("error" to "content not found: ${args.contentUrn}"))
    },
    manifest.tool("list_metadata_scopes") {
      ToolResult(
        agentContext.scopes.map { scope ->
          buildMap {
            put("scopeUrn", scope.scopeUrn)
            put("access", scope.access)
            scope.label?.let { put("label", it) }
          }
        },
      )
    },
    manifest.tool("list_entity_facets") { request ->
      val args = request.argumentsAs<ListEntityFacetsArgs>()
      ToolResult(port.listEntityFacets(args.metadataEntityId, args.scope, args.context, args.origin))
    },
    manifest.tool("validate_facet_payload") { request ->
      val args = request.argumentsAs<ValidateFacetPayloadArgs>()
      val errs = MetadataFacetValidation.validate(port, args.facetTypeKey, args.payload, args.metadataEntityId)
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
  private val agentContext: AgentContext,
  private val port: MetadataReadPort,
) : Capability {

  private val manifest = CapabilityManifest.load("capabilities/metadata-authoring.yaml")

  override val prompts: List<PromptAsset> = manifest.allPrompts

  override val protocols: List<ProtocolDefinition> = manifest.allProtocols

  override val tools: List<ToolBinding> = listOf(
    manifest.tool("propose_facet_assignment", kindOverride = ToolKind.CAPTURE) { request ->
      val args = request.argumentsAs<ProposeFacetAssignmentArgs>()
      val payloadErrors = MetadataFacetValidation.validate(
        port,
        args.facetTypeKey,
        args.payload,
        args.metadataEntityId,
      )
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
            writeScopeUrns = agentContext.writeScopeUrns(),
          ),
        )
      }
    },
  )
}

private fun matchesApplicableTo(manifest: FacetTypeManifest, targetType: String): Boolean {
  val app = manifest.applicableTo
  return app.isNullOrEmpty() || app.any { it.equals(targetType, ignoreCase = true) }
}

private fun FacetTypeManifest.toSummaryMap(): Map<String, Any?> =
  buildMap {
    put("facetTypeKey", typeKey)
    put("title", title)
    put("description", description)
    category?.let { put("category", it) }
    applicableTo?.let { put("applicableTo", it) }
    put("targetCardinality", targetCardinality.name)
  }

private fun FacetTypeManifest.toFullMap(): Map<String, Any?> =
  buildMap {
    put("facetTypeKey", typeKey)
    put("title", title)
    put("description", description)
    category?.let { put("category", it) }
    put("enabled", enabled)
    put("mandatory", mandatory)
    put("targetCardinality", targetCardinality.name)
    applicableTo?.let { put("applicableTo", it) }
    schemaVersion?.let { put("schemaVersion", it) }
    put("contentSchema", payload)
  }

internal fun validateFacetPayloadInternal(
  port: MetadataReadPort,
  facetTypeKey: String,
  payload: Map<String, Any?>,
): List<String> = MetadataFacetValidation.validate(port, facetTypeKey, payload, metadataEntityId = null)
