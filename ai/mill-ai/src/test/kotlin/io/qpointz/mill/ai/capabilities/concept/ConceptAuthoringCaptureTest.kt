package io.qpointz.mill.ai.capabilities.concept

import io.qpointz.mill.ai.capabilities.metadata.FacetCategoryWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataAuthoringCapabilityProvider
import io.qpointz.mill.ai.capabilities.metadata.MetadataCapabilityDependency
import io.qpointz.mill.ai.capabilities.metadata.MetadataContentWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataFacetProposalCapture
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.tool.ToolRequest
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.AgentContextScope
import io.qpointz.mill.data.schema.SchemaEntityTypeUrns
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadField
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.fixtures.ConceptModelFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConceptAuthoringCaptureTest {

    private val conceptFacet = FacetTypeManifest(
        typeKey = MetadataUrns.FACET_TYPE_CONCEPT,
        title = "Concept",
        description = "Business concept",
        category = "general",
        applicableTo = listOf(SchemaEntityTypeUrns.MODEL),
        targetCardinality = FacetTargetCardinality.MULTIPLE,
        payload = FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = "Concept payload",
            description = "Concept assignment payload",
            fields = listOf(
                FacetPayloadField(
                    name = "concepts",
                    required = true,
                    schema = FacetPayloadSchema(
                        type = FacetSchemaType.ARRAY,
                        title = "Concepts",
                        description = "One concept entry",
                        items = FacetPayloadSchema(
                            type = FacetSchemaType.OBJECT,
                            title = "Concept entry",
                            description = "Concept definition",
                            fields = listOf(
                                FacetPayloadField(
                                    name = "name",
                                    required = true,
                                    schema = FacetPayloadSchema(
                                        type = FacetSchemaType.STRING,
                                        title = "Name",
                                        description = "Name",
                                    ),
                                ),
                                FacetPayloadField(
                                    name = "description",
                                    required = true,
                                    schema = FacetPayloadSchema(
                                        type = FacetSchemaType.STRING,
                                        title = "Description",
                                        description = "Description",
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val port = object : MetadataReadPort {
        override fun listFacetTypes(): List<FacetTypeManifest> = listOf(conceptFacet)

        override fun getFacetType(facetTypeKey: String): FacetTypeManifest? =
            if (facetTypeKey == "concept" || facetTypeKey == MetadataUrns.FACET_TYPE_CONCEPT) conceptFacet else null

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
        ): List<String> = io.qpointz.mill.ai.capabilities.metadata.MetadataFacetValidation.validate(
            this,
            facetTypeKey,
            payload,
            metadataEntityId,
        )
    }

    private val scopes = listOf(
        AgentContextScope(MetadataUrns.scopeChat("chat-42"), "rw", "Chat"),
    )

    private fun authoringCapability() =
        MetadataAuthoringCapabilityProvider().create(
            AgentContext(contextType = "general", chatId = "chat-42", scopes = scopes),
            CapabilityDependencies.of(MetadataCapabilityDependency(port)),
        )

    @Test
    fun shouldProposeConceptFacet_onModelRootCatalogAlias() {
        val tool = authoringCapability().tools.single { it.spec.name() == "propose_facet_assignment" }
        val payload = mapOf(
            "conceptRef" to "urn:mill/model/concept:loyal-customers",
            "concepts" to listOf(
                mapOf(
                    "name" to "Loyal Customers",
                    "description" to "Customers with repeat purchases in the last year.",
                    "tags" to listOf("customer", "loyalty"),
                ),
            ),
        )
        val result = tool.handler.invoke(
            ToolRequest(
                arguments = mapOf(
                    "facetTypeKey" to "concept",
                    "catalogPath" to "model-entity",
                    "payload" to payload,
                    "rationale" to "Captured loyal customers business concept",
                    "candidateConceptLinks" to listOf(
                        mapOf(
                            "conceptRef" to "urn:mill/model/concept:loyal-customers",
                            "targetRef" to "urn:mill/model/table:sales.customers",
                            "linkKind" to "grounding",
                        ),
                    ),
                ),
            ),
        ).content as MetadataFacetProposalCapture

        assertThat(result.metadataEntityId).isEqualTo(ConceptModelFixtures.MODEL_ENTITY_ID)
        assertThat(result.catalogPath).isEqualTo("model-entity")
        assertThat(result.serializedPayload).isEqualTo(payload)
        assertThat(result.candidateConceptLinks).hasSize(1)
        assertThat(result.candidateConceptLinks.single()["conceptRef"])
            .isEqualTo("urn:mill/model/concept:loyal-customers")
        assertThat(result.writeScopeUrns).contains(MetadataUrns.scopeChat("chat-42"))
    }

    @Test
    fun shouldKeepConceptCapturePrompt_withoutMetadataAuthoringIntentOwnership() {
        val capture = CapabilityManifest.load("capabilities/concept.yaml").promptAsset("concept.capture")
        val authoringIntent = CapabilityManifest.load("capabilities/metadata-authoring.yaml")
            .promptAsset("metadata-authoring.intent")
        assertTrue(capture.content.contains("facet-assignment capture"))
        assertTrue(capture.content.contains("indicative SQL"))
        assertTrue(capture.content.contains("accept or reject"))
        assertTrue(capture.content.contains("Ask the user for clarification"))
        assertTrue(capture.content.contains("conceptRef"))
        assertFalse(capture.content.contains("AUTHOR_FACET"))
        assertFalse(authoringIntent.content.contains("CONCEPT_DEFINE"))
    }

    @Test
    fun shouldParseCandidateConceptLinks_fromToolArguments() {
        val links = ConceptCandidateLink.parseAll(
            listOf(
                mapOf(
                    "conceptRef" to ConceptModelFixtures.VIP_PASSENGERS_REF,
                    "targetRef" to "urn:mill/model/table:skymill.passenger",
                ),
                mapOf("conceptRef" to "missing-target"),
            ),
        )
        assertThat(links).hasSize(1)
        assertThat(links.single().conceptRef).isEqualTo(ConceptModelFixtures.VIP_PASSENGERS_REF)
    }
}
