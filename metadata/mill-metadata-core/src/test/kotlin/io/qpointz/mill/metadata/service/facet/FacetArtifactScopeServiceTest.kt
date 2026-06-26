package io.qpointz.mill.metadata.service.facet

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.InMemoryFacetRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryMetadataEntityRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class FacetArtifactScopeServiceTest {

    private lateinit var facets: InMemoryFacetRepository
    private lateinit var definitions: InMemoryFacetTypeDefinitionRepository
    private lateinit var service: FacetArtifactScopeService

    @BeforeEach
    fun setUp() {
        facets = InMemoryFacetRepository()
        definitions = InMemoryFacetTypeDefinitionRepository()
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
        service = FacetArtifactScopeService(
            facets,
            definitions,
            InMemoryFacetTypeRepository(),
            InMemoryMetadataEntityRepository(),
        )
    }

    @Test
    fun shouldCreateEntityRow_whenMissingBeforeAssign() {
        val entities = InMemoryMetadataEntityRepository()
        service = FacetArtifactScopeService(
            facets,
            definitions,
            InMemoryFacetTypeRepository(),
            entities,
        )

        service.assignFromProposal(
            sourceArtifactId = "art-entity",
            metadataEntityId = "urn:mill/model/attribute:skymill.passenger.id",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "Passenger id"),
            writeScopeUrns = listOf(MetadataUrns.scopeChat("chat-1")),
        )

        assertThat(entities.findById("urn:mill/model/attribute:skymill.passenger.id")).isNotNull
        assertThat(facets.findBySourceArtifactId("art-entity")).hasSize(1)
    }

    @Test
    fun shouldAssignScopeRows_whenWriteScopeUrnsPresent() {
        service.assignFromProposal(
            sourceArtifactId = "art-1",
            metadataEntityId = "urn:mill/model/table:sales.customers",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "VIP"),
            writeScopeUrns = listOf(MetadataUrns.scopeChat("chat-1")),
        )

        val rows = facets.findBySourceArtifactId("art-1")
        assertThat(rows).hasSize(1)
        assertThat(rows.single().mergeAction).isEqualTo(MergeAction.SET)
        assertThat(rows.single().sourceArtifactId).isEqualTo("art-1")
    }

    @Test
    fun shouldSkipAssign_whenWriteScopeUrnsEmpty() {
        service.assignFromProposal(
            sourceArtifactId = "art-1",
            metadataEntityId = "urn:mill/model/table:sales.customers",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "VIP"),
            writeScopeUrns = emptyList(),
        )

        assertThat(facets.findBySourceArtifactId("art-1")).isEmpty()
    }

    @Test
    fun shouldAssignAgain_whenOnlyTombstoneRowsExist() {
        service.assignFromProposal(
            sourceArtifactId = "art-1",
            metadataEntityId = "urn:mill/model/table:sales.customers",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "VIP"),
            writeScopeUrns = listOf(MetadataUrns.scopeChat("chat-1")),
        )
        service.retractBySourceArtifactId("art-1")
        assertThat(facets.findBySourceArtifactId("art-1").single().mergeAction).isEqualTo(MergeAction.TOMBSTONE)

        service.assignFromProposal(
            sourceArtifactId = "art-1",
            metadataEntityId = "urn:mill/model/table:sales.customers",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "VIP again"),
            writeScopeUrns = listOf(MetadataUrns.scopeChat("chat-1")),
        )

        val rows = facets.findBySourceArtifactId("art-1")
        assertThat(rows.filter { it.mergeAction == MergeAction.SET }).hasSize(1)
        assertThat(rows.filter { it.mergeAction == MergeAction.SET }.single().payload["summary"]).isEqualTo("VIP again")
    }

    @Test
    fun shouldTombstoneRows_onRetract() {
        service.assignFromProposal(
            sourceArtifactId = "art-1",
            metadataEntityId = "urn:mill/model/table:sales.customers",
            facetTypeKey = "descriptive",
            payload = mapOf("summary" to "VIP"),
            writeScopeUrns = listOf(MetadataUrns.scopeChat("chat-1")),
        )

        service.retractBySourceArtifactId("art-1")

        val rows = facets.findBySourceArtifactId("art-1")
        assertThat(rows).hasSize(1)
        assertThat(rows.single().mergeAction).isEqualTo(MergeAction.TOMBSTONE)
    }
}
