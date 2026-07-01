package io.qpointz.mill.ai.data.concept

import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.data.metadata.ServiceMetadataReadPort
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ServiceConceptCatalogAdapterTest {

    private lateinit var catalog: ServiceConceptCatalogAdapter
    private val scope = ConceptModelFixtures.defaultWriteScope()

    @BeforeEach
    fun setUp() {
        catalog = ServiceConceptCatalogAdapter(metadataReadPort())
    }

    @Test
    fun shouldReturnVipPassengers_fromModelConceptFacets() {
        val detail = catalog.getConcept(ConceptModelFixtures.VIP_PASSENGERS_REF, scope)
        assertThat(detail).isNotNull
        assertThat(detail!!.name).isEqualTo("VIP Passengers")
        assertThat(detail.tags).contains("passenger", "premium", "travel")
    }

    @Test
    fun shouldListDistinctTags_withCounts() {
        val tags = catalog.listConceptTags(scope)
        assertThat(tags.map { it.tag }).contains("passenger", "segmentation", "orders")
        assertThat(tags.first { it.tag == "passenger" }.count).isEqualTo(1)
    }

    @Test
    fun shouldFilterListConcepts_byExactTag() {
        val rows = catalog.listConcepts(tag = "marketing", scope = scope)
        assertThat(rows).hasSize(1)
        assertThat(rows.single().slug).isEqualTo("premium-customers")
    }

    @Test
    fun shouldSearchConcepts_lexically() {
        val rows = catalog.searchConcepts("vip", scope = scope)
        assertThat(rows.map { it.slug }).contains("vip-passengers")
    }

    @Test
    fun shouldReturnAllModelConcepts() {
        assertThat(catalog.getModelConcepts(scope)).hasSize(3)
    }

    private fun metadataReadPort(): MetadataReadPort {
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
        val facetCatalog = DefaultFacetCatalog(definitions, InMemoryFacetTypeRepository())
        val facetRepository = InMemoryFacetRepository()
        ConceptModelFixtures.seed(facetRepository)
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
