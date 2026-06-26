package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.AgentContextScope
import io.qpointz.mill.data.schema.SchemaEntityTypeUrns
import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadField
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import io.qpointz.mill.ai.core.tool.ToolRequest
import io.qpointz.mill.ai.core.tool.ToolResult

class MetadataCapabilitiesTest {

  private val tableFacet = FacetTypeManifest(
    typeKey = "urn:mill/metadata/facet-type:relation-source",
    title = "Relation source",
    description = "Outbound relation",
    category = "relation",
    applicableTo = listOf(SchemaEntityTypeUrns.TABLE),
    targetCardinality = FacetTargetCardinality.MULTIPLE,
    payload = FacetPayloadSchema(
      type = FacetSchemaType.OBJECT,
      title = "Relation",
      description = "Relation payload",
      fields = listOf(
        FacetPayloadField(
          name = "joinSql",
          required = true,
          schema = FacetPayloadSchema(type = FacetSchemaType.STRING, title = "joinSql", description = "join"),
        ),
      ),
    ),
  )

  private val port = object : MetadataReadPort {
    override fun listFacetTypes(): List<FacetTypeManifest> = listOf(tableFacet)

    override fun getFacetType(facetTypeKey: String): FacetTypeManifest? = tableFacet

    override fun listEntityFacets(
      metadataEntityId: String,
      scope: String?,
      context: String?,
      origin: String?,
    ): List<Map<String, Any?>> = emptyList()

    override fun listContent(targetUrn: String?, contentKind: String?): List<MetadataContentWire> =
      if (contentKind == MetadataContent.KIND_FACET_TYPE_EXAMPLE) {
        listOf(
          MetadataContentWire(
            contentUrn = "urn:mill/metadata/content:example-1",
            contentKind = MetadataContent.KIND_FACET_TYPE_EXAMPLE,
            targetUrn = tableFacet.typeKey,
            content = mapOf("metadataEntityId" to "urn:mill/model/table:sales.orders", "payload" to emptyMap<String, Any?>()),
            mediaType = MetadataContent.MEDIA_TYPE_JSON,
          ),
        )
      } else {
        emptyList()
      }

    override fun getContent(contentUrn: String): MetadataContentWire? = null

    override fun listFacetCategories(): List<FacetCategoryWire> =
      listOf(FacetCategoryWire("relation", "Relations", guidance = mapOf("category" to "relation")))

    override fun validateFacetPayload(
      facetTypeKey: String,
      payload: Map<String, Any?>,
      metadataEntityId: String?,
    ): List<String> = MetadataFacetValidation.validate(this, facetTypeKey, payload, metadataEntityId)
  }

  private val scopes = listOf(
    AgentContextScope(MetadataUrns.SCOPE_GLOBAL, "r", "Global"),
    AgentContextScope(MetadataUrns.scopeChat("chat-1"), "rw", "Chat"),
  )

  private fun metadataCapability() =
    MetadataCapabilityProvider().create(
      AgentContext(contextType = "general", chatId = "chat-1", scopes = scopes),
      CapabilityDependencies.of(MetadataCapabilityDependency(port)),
    )

  private fun authoringCapability() =
    MetadataAuthoringCapabilityProvider().create(
      AgentContext(contextType = "general", chatId = "chat-1", scopes = scopes),
      CapabilityDependencies.of(MetadataCapabilityDependency(port)),
    )

  @Test
  fun shouldOmitContentSchema_whenListFacetTypes() {
    val tool = metadataCapability().tools.first { it.spec.name() == "list_facet_types" }
    @Suppress("UNCHECKED_CAST")
    val rows = tool.handler.invoke(ToolRequest()).content as List<Map<String, Any?>>
    assertThat(rows).hasSize(1)
    assertThat(rows[0]).doesNotContainKey("contentSchema")
    assertThat(rows[0]["facetTypeKey"]).isEqualTo(tableFacet.typeKey)
  }

  @Test
  fun shouldIncludeExamples_whenGetFacetType() {
    val tool = metadataCapability().tools.first { it.spec.name() == "get_facet_type" }
    @Suppress("UNCHECKED_CAST")
    val body = tool.handler.invoke(
      ToolRequest(arguments = mapOf("facetTypeKey" to "relation-source")),
    ).content as Map<String, Any?>
    assertThat(body["contentSchema"]).isNotNull
    assertThat(body["examples"]).isInstanceOf(List::class.java)
  }

  @Test
  fun shouldStampWriteScopeUrns_whenProposeFacetAssignmentSucceeds() {
    val tool = authoringCapability().tools.first { it.spec.name() == "propose_facet_assignment" }
    val result = tool.handler.invoke(
      ToolRequest(
        arguments = mapOf(
          "facetTypeKey" to "relation-source",
          "metadataEntityId" to "urn:mill/model/table:sales.orders",
          "payload" to mapOf("joinSql" to "orders.id = customers.id"),
        ),
      ),
    ).content as MetadataFacetProposalCapture
    assertThat(result.writeScopeUrns).containsExactly(MetadataUrns.scopeChat("chat-1"))
  }

  @Test
  fun shouldRejectApplicableToMismatch_whenValidateFacetPayload() {
    val tool = metadataCapability().tools.first { it.spec.name() == "validate_facet_payload" }
    @Suppress("UNCHECKED_CAST")
    val body = tool.handler.invoke(
      ToolRequest(
        arguments = mapOf(
          "facetTypeKey" to "relation-source",
          "metadataEntityId" to "urn:mill/model/attribute:sales.orders.id",
          "payload" to mapOf("joinSql" to "x"),
        ),
      ),
    ).content as Map<String, Any?>
    assertThat(body["valid"]).isEqualTo(false)
  }
}
