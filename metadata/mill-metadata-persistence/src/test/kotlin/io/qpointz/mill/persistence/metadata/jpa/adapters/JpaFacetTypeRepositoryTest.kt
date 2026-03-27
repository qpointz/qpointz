package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class JpaFacetTypeRepositoryTest {

    @Mock
    private lateinit var jpaRepo: MetadataFacetTypeJpaRepository

    @Mock
    private lateinit var facetRepo: MetadataFacetJpaRepository

    private lateinit var repository: JpaFacetTypeRepository

    @BeforeEach
    fun setUp() {
        repository = JpaFacetTypeRepository(jpaRepo, facetRepo)
    }

    @Test
    fun `shouldMapToDomain_whenApplicableToJsonIsEmpty`() {
        val entity = buildEntity(applicableToJson = "[]")
        val descriptor = repository.toDomain(entity)
        assertThat(descriptor.applicableTo).isNull()
    }

    @Test
    fun `shouldMapToDomain_whenApplicableToJsonIsBlank`() {
        val entity = buildEntity(applicableToJson = "  ")
        val descriptor = repository.toDomain(entity)
        assertThat(descriptor.applicableTo).isNull()
    }

    @Test
    fun `shouldMapToDomain_whenApplicableToJsonHasValues`() {
        val entity = buildEntity(
            applicableToJson = """["urn:mill/metadata/entity-type:table","urn:mill/metadata/entity-type:schema"]"""
        )
        val descriptor = repository.toDomain(entity)
        assertThat(descriptor.applicableTo).containsExactlyInAnyOrder(
            "urn:mill/metadata/entity-type:table",
            "urn:mill/metadata/entity-type:schema"
        )
    }

    @Test
    fun `shouldMapToDomain_whenContentSchemaJsonIsNull`() {
        val entity = buildEntity(contentSchemaJson = null)
        val descriptor = repository.toDomain(entity)
        assertThat(descriptor.contentSchema).isNull()
    }

    @Test
    fun `shouldMapToDomain_whenContentSchemaJsonIsPresent`() {
        val entity = buildEntity(contentSchemaJson = """{"type":"string","maxLength":255}""")
        val descriptor = repository.toDomain(entity)
        assertThat(descriptor.contentSchema).isNotNull
        assertThat(descriptor.contentSchema!!["type"]).isEqualTo("string")
        assertThat(descriptor.contentSchema!!["maxLength"]).isEqualTo(255)
    }

    @Test
    fun `shouldMapToDomain_whenScalarFieldsArePreserved`() {
        val now = Instant.now()
        val entity = buildEntity(
            typeRes = "urn:mill/metadata/facet-type:descriptive",
            mandatory = true,
            enabled = false,
            displayName = "Descriptive",
            description = "Describes entities",
            version = "1.0",
            createdAt = now,
            updatedAt = now,
            createdBy = "alice",
            updatedBy = "bob"
        )
        val descriptor = repository.toDomain(entity)
        assertThat(descriptor.typeKey).isEqualTo("urn:mill/metadata/facet-type:descriptive")
        assertThat(descriptor.mandatory).isTrue()
        assertThat(descriptor.enabled).isFalse()
        assertThat(descriptor.displayName).isEqualTo("Descriptive")
        assertThat(descriptor.description).isEqualTo("Describes entities")
        assertThat(descriptor.version).isEqualTo("1.0")
        assertThat(descriptor.createdAt).isEqualTo(now)
        assertThat(descriptor.updatedAt).isEqualTo(now)
        assertThat(descriptor.createdBy).isEqualTo("alice")
        assertThat(descriptor.updatedBy).isEqualTo("bob")
    }

    @Test
    fun `shouldMapToEntity_whenApplicableToIsNull`() {
        val descriptor = buildDescriptor(applicableTo = null)
        val entity = repository.toEntity(descriptor)
        assertThat(entity.applicableToJson).isEqualTo("[]")
    }

    @Test
    fun `shouldMapToEntity_whenApplicableToIsEmpty`() {
        val descriptor = buildDescriptor(applicableTo = emptySet())
        val entity = repository.toEntity(descriptor)
        assertThat(entity.applicableToJson).isEqualTo("[]")
    }

    @Test
    fun `shouldMapToEntity_whenApplicableToHasValues`() {
        val descriptor = buildDescriptor(applicableTo = setOf("urn:mill/metadata/entity-type:table"))
        val entity = repository.toEntity(descriptor)
        assertThat(entity.applicableToJson).contains("urn:mill/metadata/entity-type:table")
    }

    @Test
    fun `shouldMapToEntity_whenContentSchemaIsNull`() {
        val descriptor = buildDescriptor(contentSchema = null)
        val entity = repository.toEntity(descriptor)
        assertThat(entity.contentSchemaJson).isNull()
    }

    @Test
    fun `shouldMapToEntity_whenContentSchemaIsPresent`() {
        val descriptor = buildDescriptor(contentSchema = mapOf("type" to "string"))
        val entity = repository.toEntity(descriptor)
        assertThat(entity.contentSchemaJson).contains("\"type\"")
        assertThat(entity.contentSchemaJson).contains("\"string\"")
    }

    @Test
    fun `shouldRoundTrip_whenApplicableToHasMultipleValues`() {
        val original = buildDescriptor(
            applicableTo = setOf(
                "urn:mill/metadata/entity-type:table",
                "urn:mill/metadata/entity-type:schema"
            )
        )
        val entity = repository.toEntity(original)
        val restored = repository.toDomain(entity)
        assertThat(restored.applicableTo).isEqualTo(original.applicableTo)
    }

    @Test
    fun `shouldRoundTrip_whenContentSchemaHasNestedValues`() {
        val schema = mapOf<String, Any?>("type" to "object", "properties" to mapOf("title" to mapOf("type" to "string")))
        val original = buildDescriptor(contentSchema = schema)
        val entity = repository.toEntity(original)
        val restored = repository.toDomain(entity)
        assertThat(restored.contentSchema!!["type"]).isEqualTo("object")
    }

    @Test
    fun `shouldSave_whenDescriptorIsValid`() {
        val descriptor = buildDescriptor()
        whenever(jpaRepo.findByTypeRes(descriptor.typeKey)).thenReturn(Optional.empty())
        whenever(jpaRepo.save(any<MetadataFacetTypeEntity>())).thenAnswer { it.arguments[0] as MetadataFacetTypeEntity }
        repository.save(descriptor)
        verify(jpaRepo).save(any<MetadataFacetTypeEntity>())
    }

    @Test
    fun `shouldReturnEmpty_whenTypeKeyNotFound`() {
        whenever(jpaRepo.findByTypeRes("missing")).thenReturn(Optional.empty())
        val result = repository.findByTypeKey("missing")
        assertThat(result).isEmpty
    }

    @Test
    fun `shouldReturnDescriptor_whenTypeKeyFound`() {
        val entity = buildEntity(typeRes = "urn:mill/metadata/facet-type:descriptive")
        whenever(jpaRepo.findByTypeRes("urn:mill/metadata/facet-type:descriptive")).thenReturn(Optional.of(entity))
        val result = repository.findByTypeKey("urn:mill/metadata/facet-type:descriptive")
        assertThat(result).isPresent
        assertThat(result.get().typeKey).isEqualTo("urn:mill/metadata/facet-type:descriptive")
    }

    @Test
    fun `shouldReturnAll_whenRepositoryHasEntries`() {
        val entities = listOf(
            buildEntity(typeRes = "urn:mill/metadata/facet-type:descriptive"),
            buildEntity(typeRes = "urn:mill/metadata/facet-type:governance")
        )
        whenever(jpaRepo.findAll()).thenReturn(entities)
        val result = repository.findAll()
        assertThat(result).hasSize(2)
    }

    @Test
    fun `shouldReturnTrue_whenTypeKeyExists`() {
        whenever(jpaRepo.existsByTypeRes("urn:mill/metadata/facet-type:descriptive")).thenReturn(true)
        assertThat(repository.existsByTypeKey("urn:mill/metadata/facet-type:descriptive")).isTrue()
    }

    @Test
    fun `shouldReturnFalse_whenTypeKeyAbsent`() {
        whenever(jpaRepo.existsByTypeRes("missing")).thenReturn(false)
        assertThat(repository.existsByTypeKey("missing")).isFalse()
    }

    @Test
    fun `shouldDeleteByTypeKey_whenCalled`() {
        val entity = buildEntity(typeRes = "urn:mill/metadata/facet-type:descriptive")
        whenever(jpaRepo.findByTypeRes("urn:mill/metadata/facet-type:descriptive")).thenReturn(Optional.of(entity))
        repository.deleteByTypeKey("urn:mill/metadata/facet-type:descriptive")
        verify(jpaRepo).delete(entity)
    }

    private fun buildEntity(
        typeRes: String = "urn:mill/metadata/facet-type:descriptive",
        mandatory: Boolean = false,
        enabled: Boolean = true,
        displayName: String? = "Descriptive",
        description: String? = null,
        applicableToJson: String = "[]",
        version: String? = null,
        contentSchemaJson: String? = null,
        manifestJson: String = "{}",
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
        createdBy: String? = null,
        updatedBy: String? = null
    ) = MetadataFacetTypeEntity(
        facetTypeDefId = 1L,
        typeRes = typeRes,
        mandatory = mandatory,
        enabled = enabled,
        displayName = displayName,
        description = description,
        applicableToJson = applicableToJson,
        version = version,
        contentSchemaJson = contentSchemaJson,
        manifestJson = manifestJson,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy
    )

    private fun buildDescriptor(
        typeKey: String = "urn:mill/metadata/facet-type:descriptive",
        mandatory: Boolean = false,
        enabled: Boolean = true,
        displayName: String? = "Descriptive",
        description: String? = null,
        applicableTo: Set<String>? = null,
        version: String? = null,
        contentSchema: Map<String, Any?>? = null,
        manifestJson: String? = null
    ) = FacetTypeDescriptor(
        typeKey = typeKey,
        mandatory = mandatory,
        enabled = enabled,
        displayName = displayName,
        description = description,
        applicableTo = applicableTo,
        version = version,
        contentSchema = contentSchema,
        manifestJson = manifestJson
    )
}
