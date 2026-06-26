package io.qpointz.mill.metadata.events

import io.qpointz.mill.events.model.Event
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.InMemoryFacetRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryMetadataEntityRepository
import io.qpointz.mill.metadata.service.facet.FacetArtifactScopeService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant

class FacetProposalEventConsumersTest {

    @Test
    fun shouldWirePersistHandler_toScopeService() {
        val scopeService = mock<FacetArtifactScopeService>()
        val consumer = FacetProposalEventConsumers(scopeService).consumer()

        val payload = FacetProposalPersistedPayload(
            artifactId = "art-1",
            conversationId = "chat-1",
            kind = "metadata.faceting.capture",
            metadataEntityId = "urn:mill/model/table:sales.customers",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "VIP"),
            writeScopeUrns = listOf(MetadataUrns.scopeChat("chat-1")),
        )
        val subscription = consumer.subscriptions().single {
            it.type == MetadataEventTypes.FACET_PROPOSAL_PERSISTED
        }
        subscription.handler.onEvent(
            Event(
                eventId = "e1",
                type = MetadataEventTypes.FACET_PROPOSAL_PERSISTED,
                payload = payload,
                correlationId = "art-1",
            ),
        )

        verify(scopeService).assignFromProposal(
            sourceArtifactId = "art-1",
            metadataEntityId = "urn:mill/model/table:sales.customers",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "VIP"),
            writeScopeUrns = listOf(MetadataUrns.scopeChat("chat-1")),
        )
    }

    private lateinit var facets: InMemoryFacetRepository
    private lateinit var scopeService: FacetArtifactScopeService

    @BeforeEach
    fun setUpRetractFixture() {
        facets = InMemoryFacetRepository()
        val definitions = InMemoryFacetTypeDefinitionRepository()
        val now = Instant.parse("2025-01-01T00:00:00Z")
        definitions.save(
            FacetTypeDefinition(
                typeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                displayName = "Descriptive",
                description = null,
                mandatory = false,
                enabled = true,
                targetCardinality = FacetTargetCardinality.SINGLE,
                applicableTo = null,
                contentSchema = null,
                schemaVersion = "1.0",
                createdAt = now,
                createdBy = "test",
                lastModifiedAt = now,
                lastModifiedBy = "test",
            ),
        )
        scopeService = FacetArtifactScopeService(
            facets,
            definitions,
            InMemoryFacetTypeRepository(),
            InMemoryMetadataEntityRepository(),
        )
    }

    @Test
    fun shouldTombstoneScopeRows_onRetractEvent() {
        facets.save(
            FacetAssignment(
                uid = "row-1",
                entityId = "urn:mill/model/table:sales.customers",
                facetTypeKey = "descriptive",
                scopeKey = MetadataUrns.scopeChat("chat-1"),
                mergeAction = MergeAction.SET,
                payload = mapOf("summary" to "VIP"),
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
                createdBy = "test",
                lastModifiedAt = Instant.parse("2025-01-01T00:00:00Z"),
                lastModifiedBy = "test",
                sourceArtifactId = "art-1",
            ),
        )

        val consumer = FacetProposalEventConsumers(scopeService).consumer()
        val subscription = consumer.subscriptions().single {
            it.type == MetadataEventTypes.FACET_PROPOSAL_RETRACTED
        }
        subscription.handler.onEvent(
            Event(
                eventId = "e2",
                type = MetadataEventTypes.FACET_PROPOSAL_RETRACTED,
                payload = FacetProposalRetractedPayload(
                    artifactId = "art-1",
                    conversationId = "chat-1",
                    kind = "metadata.faceting.capture",
                ),
                correlationId = "art-1",
            ),
        )

        assertThat(facets.findBySourceArtifactId("art-1").single().mergeAction).isEqualTo(MergeAction.TOMBSTONE)
    }
}
