package io.qpointz.mill.ai.data.concept

import io.qpointz.mill.ai.capabilities.metadata.MetadataAuthoringCapabilityProvider
import io.qpointz.mill.ai.capabilities.metadata.MetadataCapabilityDependency
import io.qpointz.mill.ai.capabilities.metadata.MetadataFacetProposalCapture
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.tool.ToolRequest
import io.qpointz.mill.ai.data.metadata.ServiceMetadataReadPort
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.AgentContextScope
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.fixtures.ConceptModelFixtures
import io.qpointz.mill.metadata.repository.InMemoryFacetRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryMetadataContentRepository
import io.qpointz.mill.metadata.service.DefaultFacetCatalog
import io.qpointz.mill.metadata.service.DefaultFacetService
import io.qpointz.mill.metadata.service.FacetInstanceReadMerge
import io.qpointz.mill.metadata.service.MetadataReader
import io.qpointz.mill.metadata.source.RepositoryMetadataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ConceptAuthoringFlowTest {

    @Test
    fun shouldReadCapturedConcept_afterProposeAndPersist() {
        val scope = MetadataUrns.scopeChat("capture-flow")
        val now = Instant.parse("2026-06-30T12:00:00Z")
        val facetRepository = InMemoryFacetRepository()
        val metadataReadPort = metadataReadPort(facetRepository, now)
        val catalog = ServiceConceptCatalogAdapter(metadataReadPort)

        val proposal = proposeConcept(
            metadataReadPort = metadataReadPort,
            scope = scope,
            conceptRef = "urn:mill/model/concept:loyal-customers",
        )
        facetRepository.save(
            FacetAssignment(
                uid = UUID.randomUUID().toString(),
                entityId = proposal.metadataEntityId,
                facetTypeKey = MetadataUrns.FACET_TYPE_CONCEPT,
                scopeKey = scope,
                mergeAction = MergeAction.SET,
                payload = proposal.serializedPayload,
                createdAt = now,
                createdBy = "test",
                lastModifiedAt = now,
                lastModifiedBy = "test",
            ),
        )

        val detail = catalog.getConcept("urn:mill/model/concept:loyal-customers", scope)
        assertThat(detail).isNotNull
        assertThat(detail!!.name).isEqualTo("Loyal Customers")
    }

    private fun proposeConcept(
        metadataReadPort: io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort,
        scope: String,
        conceptRef: String,
    ): MetadataFacetProposalCapture {
        val capability = MetadataAuthoringCapabilityProvider().create(
            AgentContext(
                contextType = "general",
                chatId = "capture-flow-chat",
                scopes = listOf(AgentContextScope(scope, "rw", "Chat")),
            ),
            CapabilityDependencies.of(MetadataCapabilityDependency(metadataReadPort)),
        )
        val tool = capability.tools.single { it.spec.name() == "propose_facet_assignment" }
        return tool.handler.invoke(
            ToolRequest(
                arguments = mapOf(
                    "facetTypeKey" to "concept",
                    "catalogPath" to "model-entity",
                    "payload" to mapOf(
                        "conceptRef" to conceptRef,
                        "concepts" to listOf(
                            mapOf(
                                "name" to "Loyal Customers",
                                "description" to "Customers with repeat purchases in the last year.",
                            ),
                        ),
                    ),
                ),
            ),
        ).content as MetadataFacetProposalCapture
    }

    private fun metadataReadPort(
        facetRepository: InMemoryFacetRepository,
        now: Instant,
    ): io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort {
        val definitions = InMemoryFacetTypeDefinitionRepository()
        definitions.save(
            FacetTypeDefinition(
                typeKey = MetadataUrns.FACET_TYPE_CONCEPT,
                displayName = "Concept",
                description = "Business concept",
                mandatory = false,
                enabled = true,
                targetCardinality = FacetTargetCardinality.MULTIPLE,
                applicableTo = listOf("urn:mill/metadata/entity-type:model"),
                contentSchema = null,
                schemaVersion = "1.0",
                createdAt = now,
                createdBy = "test",
                lastModifiedAt = now,
                lastModifiedBy = "test",
            ),
        )
        val facetCatalog = DefaultFacetCatalog(definitions, InMemoryFacetTypeRepository())
        val readMerge = FacetInstanceReadMerge(
            listOf(RepositoryMetadataSource(facetRepository, MetadataReader(facetCatalog))),
            facetCatalog,
        )
        val facetService = DefaultFacetService(
            facetRepository,
            facetCatalog,
            InMemoryFacetTypeRepository(),
            readMerge,
        )
        return ServiceMetadataReadPort(facetCatalog, facetService, InMemoryMetadataContentRepository())
    }
}
