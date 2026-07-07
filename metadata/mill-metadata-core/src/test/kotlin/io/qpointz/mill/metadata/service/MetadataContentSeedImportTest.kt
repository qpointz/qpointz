package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.InMemoryMetadataContentRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MetadataContentSeedImportTest {

    private val entityRepository = mock<io.qpointz.mill.metadata.repository.EntityRepository>()
    private val entityService = mock<MetadataEntityService>()
    private val facetRepository = mock<io.qpointz.mill.metadata.repository.FacetRepository>()
    private val scopeRepository = mock<io.qpointz.mill.metadata.repository.MetadataScopeRepository>()
    private val facetCatalog = mock<FacetCatalog>()
    private val contentRepository = InMemoryMetadataContentRepository()

    private lateinit var service: DefaultMetadataImportService

    @BeforeEach
    fun setUp() {
        service = DefaultMetadataImportService(
            entityRepository,
            entityService,
            facetRepository,
            scopeRepository,
            facetCatalog,
            contentRepository,
        )
    }

    @Test
    fun shouldImportPlatformCategoryGuidanceSeeds() {
        val yaml = requireNotNull(
            javaClass.classLoader.getResourceAsStream("metadata/platform-facet-category-guidance.yaml"),
        )
        val result = service.import(yaml, ImportMode.MERGE, "test")
        assertThat(result.errors).isEmpty()
        val categories = contentRepository.findByTarget(
            MetadataUrns.facetTypeCategory("general"),
            MetadataContent.KIND_FACET_TYPE_CATEGORY,
        )
        assertThat(categories).isNotEmpty
    }

    @Test
    fun shouldImportPlatformExamples_afterFacetTypesKnown() {
        val knownTypes = setOf(
            MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            MetadataUrns.FACET_TYPE_AI_ANNOTATION,
            "urn:mill/metadata/facet-type:relation-source",
            "urn:mill/metadata/facet-type:relation-target",
            "urn:mill/metadata/facet-type:dq-null-check",
            "urn:mill/metadata/facet-type:dq-predicate",
        )
        org.mockito.kotlin.whenever(facetCatalog.listDefinitions()).thenReturn(
            knownTypes.map {
                io.qpointz.mill.metadata.domain.FacetTypeDefinition(
                    typeKey = it,
                    displayName = it,
                    description = null,
                    category = "test",
                    mandatory = false,
                    enabled = true,
                    targetCardinality = io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality.SINGLE,
                    applicableTo = emptyList(),
                    contentSchema = null,
                    schemaVersion = null,
                    createdAt = java.time.Instant.EPOCH,
                    createdBy = "test",
                    lastModifiedAt = java.time.Instant.EPOCH,
                    lastModifiedBy = "test",
                )
            },
        )
        val yaml = requireNotNull(
            javaClass.classLoader.getResourceAsStream("metadata/platform-facet-authoring-examples.yaml"),
        )
        val result = service.import(yaml, ImportMode.MERGE, "test")
        assertThat(result.errors).isEmpty()
        assertThat(contentRepository.findAll()).hasSizeGreaterThanOrEqualTo(5)
    }
}
