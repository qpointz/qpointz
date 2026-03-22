package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataChangeEvent
import io.qpointz.mill.metadata.domain.MetadataChangeObserver
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream

class DefaultMetadataImportServiceTest {

    private lateinit var repository: MetadataRepository
    private lateinit var facetTypeRepo: FacetTypeRepository
    private lateinit var observer: MetadataChangeObserver
    private lateinit var service: DefaultMetadataImportService

    @BeforeEach
    fun setUp() {
        repository = mock()
        facetTypeRepo = mock()
        observer = mock()
        service = DefaultMetadataImportService(repository, facetTypeRepo, observer)
        whenever(facetTypeRepo.findAll()).thenReturn(emptyList())
    }

    // ── Legacy short-key normalisation ────────────────────────────────────────

    @Test
    fun shouldNormaliseLegacyShortKeys_whenImportingWithShortFacetTypeAndScopeKeys() {
        val yaml = """
            entities:
              - id: moneta
                type: SCHEMA
                schemaName: moneta
                facets:
                  descriptive:
                    global:
                      displayName: Moneta
        """.trimIndent()

        val resource = ByteArrayInputStream(yaml.toByteArray())
        val result = service.import(resource, ImportMode.MERGE, "test")

        assertThat(result.entitiesImported).isEqualTo(1)
        assertThat(result.errors).isEmpty()

        val captor = argumentCaptor<io.qpointz.mill.metadata.domain.MetadataEntity>()
        verify(repository).save(captor.capture())
        val entity = captor.firstValue
        assertThat(entity.facets).containsKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        val scopeMap = entity.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]
        assertThat(scopeMap).containsKey(MetadataUrns.SCOPE_GLOBAL)
    }

    // ── Custom facet-type registration ────────────────────────────────────────

    @Test
    fun shouldRegisterCustomFacetType_whenFacetTypesSection() {
        val yaml = """
            facet-types:
              - typeKey: governance
                displayName: Governance
                mandatory: false
                enabled: true
            ---
            entities:
              - id: myschema
                type: SCHEMA
                schemaName: myschema
                facets: {}
        """.trimIndent()

        val resource = ByteArrayInputStream(yaml.toByteArray())
        val result = service.import(resource, ImportMode.MERGE, "test")

        assertThat(result.facetTypesImported).isEqualTo(1)
        val captor = argumentCaptor<FacetTypeDescriptor>()
        verify(facetTypeRepo).save(captor.capture())
        assertThat(captor.firstValue.typeKey).isEqualTo("urn:mill/metadata/facet-type:governance")
    }

    // ── REPLACE mode ──────────────────────────────────────────────────────────

    @Test
    fun shouldDeleteAll_whenReplaceModeUsed() {
        val yaml = """
            entities:
              - id: e1
                type: SCHEMA
                schemaName: s1
                facets: {}
        """.trimIndent()

        val resource = ByteArrayInputStream(yaml.toByteArray())
        service.import(resource, ImportMode.REPLACE, "test")

        verify(repository).deleteAll()
        verify(repository).save(any())
    }

    @Test
    fun shouldNotDeleteAll_whenMergeModeUsed() {
        val yaml = """
            entities:
              - id: e1
                type: SCHEMA
                schemaName: s1
                facets: {}
        """.trimIndent()

        val resource = ByteArrayInputStream(yaml.toByteArray())
        service.import(resource, ImportMode.MERGE, "test")

        verify(repository, never()).deleteAll()
    }

    @Test
    fun shouldDeleteAllOnlyOnce_whenReplaceMultipleDocuments() {
        val yaml = """
            entities:
              - id: e1
                type: SCHEMA
                schemaName: s1
                facets: {}
            ---
            entities:
              - id: e2
                type: SCHEMA
                schemaName: s2
                facets: {}
        """.trimIndent()

        val resource = ByteArrayInputStream(yaml.toByteArray())
        service.import(resource, ImportMode.REPLACE, "test")

        verify(repository, times(1)).deleteAll()
        verify(repository, times(2)).save(any())
    }

    // ── Error accumulation ────────────────────────────────────────────────────

    @Test
    fun shouldAccumulateErrors_whenEntityParsingFails() {
        // Provide an entity with an invalid type (not a valid MetadataType enum value)
        val yaml = """
            entities:
              - id: bad-entity
                type: INVALID_TYPE
                schemaName: x
                facets: {}
              - id: good-entity
                type: SCHEMA
                schemaName: y
                facets: {}
        """.trimIndent()

        val resource = ByteArrayInputStream(yaml.toByteArray())
        val result = service.import(resource, ImportMode.MERGE, "test")

        // bad-entity fails deserialization; good-entity should still be saved
        assertThat(result.errors).isNotEmpty()
        assertThat(result.entitiesImported).isEqualTo(1)
    }

    // ── Observer emission ─────────────────────────────────────────────────────

    @Test
    fun shouldEmitImportedEvent_whenEntitySuccessfullySaved() {
        val yaml = """
            entities:
              - id: e1
                type: SCHEMA
                schemaName: s1
                facets: {}
        """.trimIndent()

        val resource = ByteArrayInputStream(yaml.toByteArray())
        service.import(resource, ImportMode.MERGE, "actor-1")

        val captor = argumentCaptor<MetadataChangeEvent>()
        verify(observer).onEvent(captor.capture())
        val event = captor.firstValue
        assertThat(event).isInstanceOf(MetadataChangeEvent.Imported::class.java)
        val imported = event as MetadataChangeEvent.Imported
        assertThat(imported.entityId).isEqualTo("e1")
        assertThat(imported.actorId).isEqualTo("actor-1")
        assertThat(imported.mode).isEqualTo(ImportMode.MERGE)
    }

    // ── Export ────────────────────────────────────────────────────────────────

    @Test
    fun shouldExportEntitiesFilteredToRequestedScope() {
        val entity = io.qpointz.mill.metadata.domain.MetadataEntity(
            id = "e1",
            type = io.qpointz.mill.metadata.domain.MetadataType.SCHEMA,
            schemaName = "s1",
            facets = mutableMapOf(
                MetadataUrns.FACET_TYPE_DESCRIPTIVE to mutableMapOf(
                    MetadataUrns.SCOPE_GLOBAL to mapOf("displayName" to "S1"),
                    MetadataUrns.scopeUser("alice") to mapOf("displayName" to "S1-alice")
                )
            )
        )
        whenever(repository.findAll()).thenReturn(listOf(entity))

        val yaml = service.export(MetadataUrns.SCOPE_GLOBAL)

        assertThat(yaml).contains("displayName")
        assertThat(yaml).doesNotContain("alice")
    }

    @Test
    fun shouldIncludeCustomFacetTypesInExport() {
        whenever(repository.findAll()).thenReturn(emptyList())
        whenever(facetTypeRepo.findAll()).thenReturn(listOf(
            FacetTypeDescriptor(
                typeKey = "urn:mill/metadata/facet-type:governance",
                displayName = "Governance"
            )
        ))

        val yaml = service.export(MetadataUrns.SCOPE_GLOBAL)

        assertThat(yaml).contains("governance")
    }

    @Test
    fun shouldNotIncludePlatformFacetTypesInExport() {
        whenever(repository.findAll()).thenReturn(emptyList())
        whenever(facetTypeRepo.findAll()).thenReturn(listOf(
            FacetTypeDescriptor(
                typeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                displayName = "Descriptive"
            )
        ))

        val yaml = service.export(MetadataUrns.SCOPE_GLOBAL)

        // Platform types should NOT appear in the facet-types preamble section
        assertThat(yaml).doesNotContain("facet-types")
    }
}
