package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.ai.capabilities.metadata.MetadataAuthoringCapabilityProvider
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.tool.ToolRequest
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.AgentContextScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadField
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AiAnnotationAuthoringCaptureTest {

    private val aiAnnotationFacet = FacetTypeManifest(
        typeKey = MetadataUrns.FACET_TYPE_AI_ANNOTATION,
        title = "AI Annotation",
        description = "Entity-scoped agent instructions",
        category = "ai",
        applicableTo = listOf(
            "urn:mill/metadata/entity-type:schema",
            "urn:mill/metadata/entity-type:table",
            "urn:mill/metadata/entity-type:attribute",
        ),
        targetCardinality = FacetTargetCardinality.MULTIPLE,
        payload = FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = "AI annotation payload",
            description = "Agent instruction",
            fields = listOf(
                FacetPayloadField(
                    name = "instruction",
                    required = true,
                    schema = FacetPayloadSchema(
                        type = FacetSchemaType.STRING,
                        title = "Instruction",
                        description = "Agent instruction",
                    ),
                ),
            ),
        ),
    )

    private val port = object : MetadataReadPort {
        override fun listFacetTypes(): List<FacetTypeManifest> = listOf(aiAnnotationFacet)

        override fun getFacetType(facetTypeKey: String): FacetTypeManifest? =
            if (facetTypeKey == "ai-annotation" || facetTypeKey == MetadataUrns.FACET_TYPE_AI_ANNOTATION) {
                aiAnnotationFacet
            } else {
                null
            }

        override fun listEntityFacets(
            metadataEntityId: String,
            scope: String?,
            context: String?,
            origin: String?,
        ): List<Map<String, Any?>> = emptyList()

        override fun listContent(targetUrn: String?, contentKind: String?): List<MetadataContentWire> =
            emptyList()

        override fun getContent(contentUrn: String): MetadataContentWire? = null

        override fun listFacetCategories(): List<FacetCategoryWire> = emptyList()

        override fun validateFacetPayload(
            facetTypeKey: String,
            payload: Map<String, Any?>,
            metadataEntityId: String?,
        ): List<String> = MetadataFacetValidation.validate(this, facetTypeKey, payload, metadataEntityId)
    }

    private val scopes = listOf(
        AgentContextScope(MetadataUrns.scopeChat("chat-segments"), "rw", "Chat"),
    )

    private fun authoringCapability() =
        MetadataAuthoringCapabilityProvider().create(
            AgentContext(contextType = "general", chatId = "chat-segments", scopes = scopes),
            CapabilityDependencies.of(MetadataCapabilityDependency(port)),
        )

    @Test
    fun shouldProposeAiAnnotation_onSegmentsCatalogPath() {
        val tool = authoringCapability().tools.single { it.spec.name() == "propose_facet_assignment" }
        val payload = mapOf(
            "title" to "City name projection",
            "instruction" to "When this table is used in SQL, join skymill.cities twice for origin and destination and prefer city names in SELECT output instead of raw ids.",
            "kind" to "sql_generation",
        )
        val result = tool.handler.invoke(
            ToolRequest(
                arguments = mapOf(
                    "facetTypeKey" to "ai-annotation",
                    "catalogPath" to "skymill.segments",
                    "payload" to payload,
                    "rationale" to "Captured segments city-name projection rule for the agent.",
                ),
            ),
        ).content as MetadataFacetProposalCapture

        assertThat(result.facetTypeKey).isEqualTo("ai-annotation")
        assertThat(result.catalogPath).isEqualTo("skymill.segments")
        assertThat(result.serializedPayload).isEqualTo(payload)
        assertThat(result.writeScopeUrns).contains(MetadataUrns.scopeChat("chat-segments"))
    }

    @Test
    fun shouldRouteAgentInstructions_viaCatalogDrivenMetadataAuthoringPrompts() {
        val intent = CapabilityManifest.load("capabilities/metadata-authoring.yaml")
            .promptAsset("metadata-authoring.intent")
        val reasoning = CapabilityManifest.load("capabilities/metadata-authoring.yaml")
            .promptAsset("metadata-authoring.reasoning")
        val batch = CapabilityManifest.load("capabilities/metadata-authoring.yaml")
            .promptAsset("metadata-authoring.batch")
        val request = CapabilityManifest.load("capabilities/metadata-authoring.yaml")
            .promptAsset("metadata.faceting.request")
        assertTrue(intent.content.contains("agent instructions"))
        assertTrue(reasoning.content.contains("list_facet_types"))
        assertTrue(reasoning.content.contains("instruction"))
        assertFalse(reasoning.content.contains("ai-annotation"))
        assertTrue(batch.content.contains("contentSchema"))
        assertTrue(request.content.contains("facetTypeKey"))
        assertFalse(request.content.contains("ai-annotation"))
    }
}
