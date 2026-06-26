package io.qpointz.mill.ai.data.metadata

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryMetadataContentRepository
import io.qpointz.mill.metadata.service.DefaultFacetCatalog
import io.qpointz.mill.metadata.service.DefaultFacetService
import io.qpointz.mill.metadata.service.FacetInstanceReadMerge
import io.qpointz.mill.metadata.service.MetadataReader
import io.qpointz.mill.metadata.source.RepositoryMetadataSource
import io.qpointz.mill.metadata.repository.InMemoryFacetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ServiceMetadataReadPortTest {

  private lateinit var port: ServiceMetadataReadPort

  @BeforeEach
  fun setUp() {
    val now = Instant.parse("2026-01-01T00:00:00Z")
    val definitions = InMemoryFacetTypeDefinitionRepository()
    val runtime = InMemoryFacetTypeRepository()
    val catalog = DefaultFacetCatalog(definitions, runtime)
    definitions.save(
      FacetTypeDefinition(
        typeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
        displayName = "Descriptive",
        description = "Desc",
        category = "general",
        mandatory = false,
        enabled = true,
        targetCardinality = FacetTargetCardinality.SINGLE,
        applicableTo = emptyList(),
        contentSchema = mapOf(
          "type" to "OBJECT",
          "title" to "Descriptive",
          "fields" to listOf(
            mapOf(
              "name" to "description",
              "required" to true,
              "schema" to mapOf("type" to "STRING", "title" to "description"),
            ),
          ),
        ),
        schemaVersion = "1.0",
        createdAt = now,
        createdBy = "test",
        lastModifiedAt = now,
        lastModifiedBy = "test",
      ),
    )
    val facetRepository = InMemoryFacetRepository()
    val metadataReader = MetadataReader(catalog)
    val readMerge = FacetInstanceReadMerge(
      listOf(RepositoryMetadataSource(facetRepository, metadataReader)),
      catalog,
    )
    val facetService = DefaultFacetService(facetRepository, catalog, runtime, readMerge)
    val contentRepository = InMemoryMetadataContentRepository()
    contentRepository.save(
      MetadataContent(
        contentUrn = MetadataUrns.content("facet-type-category/general"),
        contentKind = MetadataContent.KIND_FACET_TYPE_CATEGORY,
        targetUrn = MetadataUrns.facetTypeCategory("general"),
        contentBody = """{"category":"general","signalPhrases":["describes"]}""",
        createdAt = now,
        createdBy = "test",
        lastModifiedAt = now,
        lastModifiedBy = "test",
      ),
    )
    port = ServiceMetadataReadPort(catalog, facetService, contentRepository)
  }

  @Test
  fun shouldListFacetTypesFromDefinitions() {
    assertThat(port.listFacetTypes()).extracting<String> { it.typeKey }
      .contains(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
  }

  @Test
  fun shouldListFacetCategoriesWithGuidance() {
    val categories = port.listFacetCategories()
    assertThat(categories.map { it.category }).contains("general")
    assertThat(categories.first { it.category == "general" }.guidance).containsEntry("category", "general")
  }

  @Test
  fun shouldValidatePayloadAgainstSchema() {
    val errors = port.validateFacetPayload(
      MetadataUrns.FACET_TYPE_DESCRIPTIVE,
      mapOf("description" to "ok"),
      null,
    )
    assertThat(errors).isEmpty()
  }
}
