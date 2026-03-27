package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataChangeEvent
import io.qpointz.mill.metadata.domain.MetadataChangeObserver
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.PlatformFacetTypeDefinitions
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
import java.nio.file.Files
import java.nio.file.Paths

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
        whenever(facetTypeRepo.existsByTypeKey(any())).thenReturn(true)
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
                title: Governance
                description: Governance metadata
                mandatory: false
                enabled: true
                payload:
                  type: OBJECT
                  title: Governance payload
                  description: Governance schema
                  fields: []
                  required: []
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

    @Test
    fun shouldImportCanonicalEnvelope_whenFacetTypesAndEntitiesProvided() {
        val yaml = """
            version: 1
            facet-types:
              - typeKey: governance
                title: Governance
                description: Governance metadata
                mandatory: false
                enabled: true
                payload:
                  type: OBJECT
                  title: Governance payload
                  description: Governance schema
                  fields: []
                  required: []
            entities:
              - id: canonical-schema
                type: SCHEMA
                schemaName: canonical
                facets: {}
        """.trimIndent()

        val result = service.import(ByteArrayInputStream(yaml.toByteArray()), ImportMode.MERGE, "test")

        assertThat(result.facetTypesImported).isEqualTo(1)
        assertThat(result.entitiesImported).isEqualTo(1)
        verify(facetTypeRepo).save(any())
        verify(repository).save(any())
    }

    @Test
    fun shouldEnsurePlatformFacetTypes_whenNoFacetTypesProvided() {
        whenever(facetTypeRepo.existsByTypeKey(any())).thenReturn(false)
        val yaml = """
            entities:
              - id: e1
                type: SCHEMA
                schemaName: s1
                facets: {}
        """.trimIndent()

        val result = service.import(ByteArrayInputStream(yaml.toByteArray()), ImportMode.MERGE, "test")

        val expected = PlatformFacetTypeDefinitions.manifests().size
        assertThat(result.facetTypesImported).isEqualTo(expected)
        verify(facetTypeRepo, times(expected)).save(any())
    }

    @Test
    fun shouldEnsurePlatformFacetTypes_whenCanonicalEnvelopeHasNoFacetTypesSection() {
        whenever(facetTypeRepo.existsByTypeKey(any())).thenReturn(false)
        val yaml = """
            version: 1
            entities:
              - id: canonical-no-facet-types
                type: SCHEMA
                schemaName: canonical_no_facets
                facets: {}
        """.trimIndent()

        val result = service.import(ByteArrayInputStream(yaml.toByteArray()), ImportMode.MERGE, "test")

        assertThat(result.entitiesImported).isEqualTo(1)
        val expected = PlatformFacetTypeDefinitions.manifests().size
        assertThat(result.facetTypesImported).isEqualTo(expected)
        verify(repository).save(any())
        verify(facetTypeRepo, times(expected)).save(any())
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

    @Test
    fun shouldMapLegacyStructuralFacetsToSourceFacets_andForceGlobalScope() {
        val yaml = """
            entities:
              - id: t1
                type: TABLE
                schemaName: s1
                tableName: t1
                facets:
                  structural:
                    user:alice:
                      physicalName: T1
              - id: t1.c1
                type: ATTRIBUTE
                schemaName: s1
                tableName: t1
                attributeName: c1
                facets:
                  structural:
                    team:data:
                      physicalName: C1
                      physicalType: VARCHAR
                      nullable: true
                      isPrimaryKey: false
                      isForeignKey: true
        """.trimIndent()

        val result = service.import(ByteArrayInputStream(yaml.toByteArray()), ImportMode.MERGE, "test")
        assertThat(result.entitiesImported).isEqualTo(2)

        val captor = argumentCaptor<io.qpointz.mill.metadata.domain.MetadataEntity>()
        verify(repository, times(2)).save(captor.capture())
        val table = captor.allValues.first { it.id == "t1" }
        val attribute = captor.allValues.first { it.id == "t1.c1" }
        val sourceTableFacet = MetadataUrns.normaliseFacetTypePath("source-table")
        val sourceColumnFacet = MetadataUrns.normaliseFacetTypePath("source-column")

        assertThat(table.facets).containsKey(sourceTableFacet)
        assertThat(table.facets[sourceTableFacet]).containsKey(MetadataUrns.SCOPE_GLOBAL)
        val tablePayload = table.facets[sourceTableFacet]?.get(MetadataUrns.SCOPE_GLOBAL) as Map<*, *>
        assertThat(tablePayload["sourceType"]).isEqualTo("FLOW")
        assertThat(tablePayload["name"]).isEqualTo("T1")

        assertThat(attribute.facets).containsKey(sourceColumnFacet)
        assertThat(attribute.facets[sourceColumnFacet]).containsKey(MetadataUrns.SCOPE_GLOBAL)
        val columnPayload = attribute.facets[sourceColumnFacet]?.get(MetadataUrns.SCOPE_GLOBAL) as Map<*, *>
        assertThat(columnPayload["name"]).isEqualTo("C1")
        assertThat(columnPayload["type"]).isEqualTo("VARCHAR")
        assertThat(columnPayload["nullable"]).isEqualTo(true)
        assertThat(columnPayload["isFK"]).isEqualTo(true)
        assertThat(columnPayload["isPK"]).isEqualTo(false)
    }

    @Test
    fun shouldMapLegacyRelationsListToRelationPayloadItems_andFallbackCardinalityToUnknown() {
        val yaml = """
            entities:
              - id: t2
                type: TABLE
                schemaName: s1
                tableName: t2
                facets:
                  relation:
                    global:
                      relations:
                        - name: t2_x
                          description: relation
                          cardinality: one_to_many
                          sourceTable: { schema: s1, table: t2 }
                          sourceAttributes: [id]
                          targetTable: { schema: s1, table: x }
                          targetAttributes: [t2_id]
                          joinSql: t2.id = x.t2_id
                        - name: t2_y
                          sourceTable: { schema: s1, table: t2 }
                          sourceAttributes: [id]
                          targetTable: { schema: s1, table: y }
                          targetAttributes: [t2_id]
                          joinSql: t2.id = y.t2_id
        """.trimIndent()

        service.import(ByteArrayInputStream(yaml.toByteArray()), ImportMode.MERGE, "test")

        val captor = argumentCaptor<io.qpointz.mill.metadata.domain.MetadataEntity>()
        verify(repository).save(captor.capture())
        val entity = captor.firstValue
        val relationPayload = entity.facets[MetadataUrns.FACET_TYPE_RELATION]?.get(MetadataUrns.SCOPE_GLOBAL) as List<*>
        assertThat(relationPayload).hasSize(2)
        val first = relationPayload[0] as Map<*, *>
        val second = relationPayload[1] as Map<*, *>
        assertThat(first["cardinality"]).isEqualTo("ONE_TO_MANY")
        assertThat(second["cardinality"]).isEqualTo("UNKNOWN")
    }

    @Test
    fun shouldImportSkymillFixture_andTransformToCurrentPlatformFacets() {
        val fixture = resolveRepoRoot()
            .resolve("test/datasets/skymill/skymill-meta-repository.yaml")
        require(Files.exists(fixture)) {
            "Missing skymill fixture at expected path: ${fixture.toAbsolutePath()}"
        }
        val yaml = Files.readString(fixture)

        val result = service.import(ByteArrayInputStream(yaml.toByteArray()), ImportMode.REPLACE, "system")

        assertThat(result.errors).isEmpty()
        assertThat(result.entitiesImported).isGreaterThan(10)
        verify(repository).deleteAll()

        val captor = argumentCaptor<io.qpointz.mill.metadata.domain.MetadataEntity>()
        verify(repository, times(result.entitiesImported)).save(captor.capture())

        val sourceTableFacet = MetadataUrns.normaliseFacetTypePath("source-table")
        val sourceColumnFacet = MetadataUrns.normaliseFacetTypePath("source-column")

        val citiesTable = captor.allValues.first { it.id == "skymill.cities" }
        val tablePayload = citiesTable.facets[sourceTableFacet]?.get(MetadataUrns.SCOPE_GLOBAL) as Map<*, *>
        assertThat(tablePayload["sourceType"]).isEqualTo("FLOW")
        assertThat(tablePayload["name"]).isEqualTo("CITIES")

        val citiesIdColumn = captor.allValues.first { it.id == "skymill.cities.id" }
        val columnPayload = citiesIdColumn.facets[sourceColumnFacet]?.get(MetadataUrns.SCOPE_GLOBAL) as Map<*, *>
        assertThat(columnPayload["name"]).isEqualTo("ID")
        assertThat(columnPayload["type"]).isEqualTo("INTEGER")
        assertThat(columnPayload["isPK"]).isEqualTo(true)
        assertThat(columnPayload["isFK"]).isEqualTo(false)

        val relationPayload = citiesTable.facets[MetadataUrns.FACET_TYPE_RELATION]?.get(MetadataUrns.SCOPE_GLOBAL) as List<*>
        assertThat(relationPayload).isNotEmpty()
        val firstRelation = relationPayload.first() as Map<*, *>
        assertThat(firstRelation["cardinality"]).isEqualTo("ONE_TO_MANY")
        assertThat((firstRelation["source"] as Map<*, *>)["columns"]).isInstanceOf(List::class.java)
        assertThat((firstRelation["target"] as Map<*, *>)["columns"]).isInstanceOf(List::class.java)
    }

    private fun resolveRepoRoot(): java.nio.file.Path {
        var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
        while (true) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) {
                return current
            }
            current = current.parent ?: break
        }
        error("Unable to locate repository root from ${System.getProperty("user.dir")}")
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
