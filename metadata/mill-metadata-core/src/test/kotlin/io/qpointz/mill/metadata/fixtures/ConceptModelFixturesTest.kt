package io.qpointz.mill.metadata.fixtures

import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.InMemoryFacetRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.service.DefaultFacetCatalog
import io.qpointz.mill.metadata.service.DefaultFacetService
import io.qpointz.mill.metadata.service.FacetInstanceReadMerge
import io.qpointz.mill.metadata.service.MetadataReader
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.metadata.source.RepositoryMetadataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ConceptModelFixturesTest {

    private lateinit var facetRepository: InMemoryFacetRepository
    private lateinit var facetService: DefaultFacetService

    @BeforeEach
    fun setUp() {
        val now = Instant.parse("2026-06-30T12:00:00Z")
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
        val catalog = DefaultFacetCatalog(definitions, InMemoryFacetTypeRepository())
        facetRepository = InMemoryFacetRepository()
        ConceptModelFixtures.seed(facetRepository)
        val readMerge = FacetInstanceReadMerge(
            listOf(RepositoryMetadataSource(facetRepository, MetadataReader(catalog))),
            catalog,
        )
        facetService = DefaultFacetService(facetRepository, catalog, InMemoryFacetTypeRepository(), readMerge)
    }

    @Test
    fun shouldLoadVipPassengersConcept_onModelEntityInWriteScope() {
        val scope = ConceptModelFixtures.defaultWriteScope()
        val rows = facetService.resolve(
            ModelEntityUrn.MODEL_ENTITY_ID,
            MetadataReadContext.parse(scope, null),
        )
        assertThat(rows).hasSize(3)
        val vip = rows.single { row ->
            @Suppress("UNCHECKED_CAST")
            val concepts = row.payload["concepts"] as? List<Map<String, Any?>>
            concepts?.firstOrNull()?.get("name") == "VIP Passengers"
        }
        assertThat(vip.payload["conceptRef"]).isEqualTo(ConceptModelFixtures.VIP_PASSENGERS_REF)
        @Suppress("UNCHECKED_CAST")
        val concept = (vip.payload["concepts"] as List<Map<String, Any?>>).single()
        assertThat(concept["tags"]).isEqualTo(listOf("passenger", "premium", "travel"))
        assertThat(concept["sql"]).asString().contains("passenger_class")
    }

    @Test
    fun shouldAssignEachConcept_toSeparateFacetRow() {
        val rows = facetRepository.findByEntityAndType(
            ModelEntityUrn.MODEL_ENTITY_ID,
            MetadataUrns.FACET_TYPE_CONCEPT,
        )
        assertThat(rows).hasSize(3)
        assertThat(rows.map { it.uid }).doesNotHaveDuplicates()
    }
}
