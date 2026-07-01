package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.ai.capabilities.concept.ConceptCandidateLink
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
  val catalogPath: String? = null,
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
  val catalogPath: String? = null,
  val metadataEntityId: String? = null,
  val scope: String? = null,
  val context: String? = null,
  val origin: String? = null,
)

private data class ValidateFacetPayloadArgs(
  val facetTypeKey: String,
  val payload: Map<String, Any?>,
  val catalogPath: String? = null,
  val metadataEntityId: String? = null,
)

private data class ProposeFacetAssignmentArgs(
  val facetTypeKey: String,
  val catalogPath: String? = null,
  val metadataEntityId: String? = null,
  val payload: Map<String, Any?>,
  val rationale: String? = null,
  val candidateConceptLinks: List<Map<String, Any?>>? = null,
)

data class MetadataFacetProposalCapture(
  val captureType: String = "facet_assignment",
  val facetTypeKey: String,
  val catalogPath: String,
  val metadataEntityId: String,
  val serializedPayload: Map<String, Any?>,
  val rationale: String?,
  val writeScopeUrns: List<String> = emptyList(),
  val validationWarnings: List<String> = emptyList(),
  val candidateConceptLinks: List<Map<String, Any?>> = emptyList(),
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
      val entityTypeUrn = resolveTargetEntity(args.catalogPath, args.metadataEntityId)
        ?.let { MetadataFacetValidation.applicableEntityTypeUrn(it.metadataEntityUrn) }
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
      val resolved = resolveTargetEntity(args.catalogPath, args.metadataEntityId)
        ?: return@tool ToolResult(mapOf("error" to "catalogPath or metadataEntityId is required"))
      val scopeParam = args.scope ?: args.context ?: agentContext.readableScopesParam()
      ToolResult(
        port.listEntityFacets(
          resolved.metadataEntityUrn,
          scopeParam,
          null,
          args.origin,
        ),
      )
    },
    manifest.tool("validate_facet_payload") { request ->
      val args = request.argumentsAs<ValidateFacetPayloadArgs>()
      val resolvedEntityId = resolveTargetEntity(args.catalogPath, args.metadataEntityId)?.metadataEntityUrn
      val errs = MetadataFacetValidation.validate(port, args.facetTypeKey, args.payload, resolvedEntityId)
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
      val raw = args.catalogPath?.trim()?.takeIf { it.isNotEmpty() }
        ?: args.metadataEntityId?.trim()?.takeIf { it.isNotEmpty() }
      if (raw == null) {
        return@tool ToolResult(
          mapOf(
            "rejected" to true,
            "errors" to listOf(
              "catalogPath or metadataEntityId is required " +
                "(qualified name such as schema.table.column, or model-entity for concept facets)",
            ),
          ),
        )
      }
      val resolved = try {
        MetadataEntityIds.resolveEntity(raw)
      } catch (ex: IllegalArgumentException) {
        return@tool ToolResult(
          mapOf(
            "rejected" to true,
            "errors" to listOf(ex.message ?: "invalid catalog path: $raw"),
          ),
        )
      }
      val payloadErrors = MetadataFacetValidation.validate(
        port,
        args.facetTypeKey,
        args.payload,
        resolved.metadataEntityUrn,
      )
      if (payloadErrors.isNotEmpty()) {
        ToolResult(
          mapOf(
            "rejected" to true,
            "errors" to payloadErrors,
          ),
        )
      } else {
        val candidateLinks = ConceptCandidateLink.parseAll(args.candidateConceptLinks)
          .map { it.toEnvelopeMap() }
        ToolResult(
          MetadataFacetProposalCapture(
            facetTypeKey = args.facetTypeKey,
            catalogPath = resolved.catalogPath,
            metadataEntityId = resolved.metadataEntityUrn,
            serializedPayload = args.payload,
            rationale = args.rationale,
            writeScopeUrns = agentContext.writeScopeUrns(),
            candidateConceptLinks = candidateLinks,
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

private fun resolveTargetEntity(
  catalogPath: String?,
  metadataEntityId: String?,
): MetadataEntityIds.ResolvedEntity? {
  val raw = catalogPath?.trim()?.takeIf { it.isNotEmpty() }
    ?: metadataEntityId?.trim()?.takeIf { it.isNotEmpty() }
    ?: return null
  return try {
    MetadataEntityIds.resolveEntity(raw)
  } catch (_: IllegalArgumentException) {
    null
  }
}
