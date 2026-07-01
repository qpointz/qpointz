package io.qpointz.mill.ai.capabilities.concept

import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.tool.ToolRequest
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.AgentContextScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.fixtures.ConceptModelFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConceptCapabilitiesTest {

    private val catalog = object : ConceptCatalogPort {
        override fun listConceptTags(scope: String?) = listOf(ConceptTagCount("passenger", 1))

        override fun listConcepts(tag: String?, scope: String?) =
            listOf(
                ConceptSummary(
                    conceptRef = ConceptModelFixtures.VIP_PASSENGERS_REF,
                    slug = "vip-passengers",
                    name = "VIP Passengers",
                    description = "Premium travelers",
                    tags = listOf("passenger"),
                ),
            )

        override fun getConcept(conceptRef: String, scope: String?) =
            ConceptDetail(
                conceptRef = ConceptModelFixtures.VIP_PASSENGERS_REF,
                slug = "vip-passengers",
                name = "VIP Passengers",
                description = "Premium travelers",
                sql = "SELECT 1",
                tags = listOf("passenger"),
                source = "MANUAL",
                sourceSession = null,
                facetUid = "facet-1",
            )

        override fun searchConcepts(query: String, tag: String?, scope: String?) = listConcepts(tag, scope)

        override fun getModelConcepts(scope: String?) = listOf(requireNotNull(getConcept(ConceptModelFixtures.VIP_PASSENGERS_REF, scope)))
    }

    private val capability = ConceptCapabilityProvider().create(
        AgentContext(
            contextType = "general",
            scopes = listOf(AgentContextScope(ConceptModelFixtures.defaultWriteScope(), "r", "w")),
        ),
        CapabilityDependencies.of(ConceptCapabilityDependency(catalog)),
    )

    @Test
    fun shouldExposeConceptQueryTools() {
        val names = capability.tools.map { it.spec.name() }.toSet()
        assertThat(names).containsExactlyInAnyOrder(
            "list_concept_tags",
            "list_concepts",
            "get_concept",
            "search_concepts",
            "get_model_concepts",
        )
    }

    @Test
    fun shouldReturnVipPassengers_fromGetConceptTool() {
        val binding = capability.tools.single { it.spec.name() == "get_concept" }
        val result = binding.handler.invoke(
            ToolRequest(mapOf("conceptRef" to ConceptModelFixtures.VIP_PASSENGERS_REF)),
        )
        @Suppress("UNCHECKED_CAST")
        val detail = result.content as ConceptDetail
        assertThat(detail.name).isEqualTo("VIP Passengers")
    }

    @Test
    fun shouldKeepConceptIntent_capabilityLocalWithoutDataQuery() {
        val intent = CapabilityManifest.load("capabilities/concept.yaml").promptAsset("concept.intent")
        assertTrue(intent.content.contains("CONCEPT_LOOKUP"))
        assertFalse(intent.content.contains("DATA_QUERY"))
        assertFalse(intent.content.contains("EXPLORE"))
        assertFalse(intent.content.contains("AUTHOR_FACET"))
    }

    @Test
    fun shouldKeepConceptSystem_withoutSchemaOrSqlToolEnumeration() {
        val system = CapabilityManifest.load("capabilities/concept.yaml").promptAsset("concept.system")
        assertFalse(system.content.contains("list_schemas"))
        assertFalse(system.content.contains("validate_sql"))
        assertFalse(system.content.contains("propose_facet_assignment"))
        assertTrue(system.content.contains("model-level concepts"))
    }

    @Test
    fun shouldEnableMcpExposure_byDefaultInManifest() {
        val manifest = CapabilityManifest.load("capabilities/concept.yaml")
        assertTrue(manifest.mcpSettings.enabled)
    }
}
