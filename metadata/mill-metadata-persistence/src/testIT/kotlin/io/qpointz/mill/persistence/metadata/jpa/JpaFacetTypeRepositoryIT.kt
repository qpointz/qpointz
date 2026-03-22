package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaFacetTypeRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaFacetTypeRepositoryIT {

    @Autowired
    private lateinit var jpaRepo: MetadataFacetTypeJpaRepository

    private val repository by lazy { JpaFacetTypeRepository(jpaRepo) }

    @Test
    fun `shouldFindPlatformFacetTypes_whenV4MigrationRan`() {
        val all = repository.findAll()
        val keys = all.map { it.typeKey }.toSet()
        assertThat(keys).contains(
            MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            MetadataUrns.FACET_TYPE_STRUCTURAL,
            MetadataUrns.FACET_TYPE_RELATION,
            MetadataUrns.FACET_TYPE_CONCEPT,
            MetadataUrns.FACET_TYPE_VALUE_MAPPING
        )
    }

    @Test
    fun `shouldPersistAndRetrieve_whenNewFacetTypeIsSaved`() {
        val descriptor = FacetTypeDescriptor(
            typeKey = "urn:mill/metadata/facet-type:governance",
            mandatory = false,
            enabled = true,
            displayName = "Governance",
            description = "Data governance annotations",
            applicableTo = setOf(MetadataUrns.ENTITY_TYPE_TABLE, MetadataUrns.ENTITY_TYPE_SCHEMA),
            version = "1.0"
        )
        repository.save(descriptor)

        val found = repository.findByTypeKey("urn:mill/metadata/facet-type:governance")
        assertThat(found).isPresent
        assertThat(found.get().displayName).isEqualTo("Governance")
        assertThat(found.get().applicableTo).containsExactlyInAnyOrder(
            MetadataUrns.ENTITY_TYPE_TABLE,
            MetadataUrns.ENTITY_TYPE_SCHEMA
        )
    }

    @Test
    fun `shouldPersistContentSchema_whenContentSchemaIsProvided`() {
        val schema = mapOf<String, Any?>(
            "type" to "object",
            "properties" to mapOf("title" to mapOf("type" to "string"))
        )
        val descriptor = FacetTypeDescriptor(
            typeKey = "urn:mill/metadata/facet-type:schema-test",
            contentSchema = schema
        )
        repository.save(descriptor)

        val found = repository.findByTypeKey("urn:mill/metadata/facet-type:schema-test")
        assertThat(found).isPresent
        assertThat(found.get().contentSchema).isNotNull
        assertThat(found.get().contentSchema!!["type"]).isEqualTo("object")
    }

    @Test
    fun `shouldReturnEmpty_whenTypeKeyAbsent`() {
        val found = repository.findByTypeKey("urn:mill/metadata/facet-type:does-not-exist")
        assertThat(found).isEmpty
    }

    @Test
    fun `shouldExistsByTypeKey_whenPlatformTypePresent`() {
        assertThat(repository.existsByTypeKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)).isTrue()
    }

    @Test
    fun `shouldNotExistsByTypeKey_whenAbsent`() {
        assertThat(repository.existsByTypeKey("urn:mill/metadata/facet-type:absent")).isFalse()
    }

    @Test
    fun `shouldDeleteByTypeKey_whenTypeExists`() {
        val descriptor = FacetTypeDescriptor(typeKey = "urn:mill/metadata/facet-type:to-delete")
        repository.save(descriptor)
        assertThat(repository.existsByTypeKey("urn:mill/metadata/facet-type:to-delete")).isTrue()

        repository.deleteByTypeKey("urn:mill/metadata/facet-type:to-delete")

        assertThat(repository.existsByTypeKey("urn:mill/metadata/facet-type:to-delete")).isFalse()
    }

    @Test
    fun `shouldRoundTrip_whenApplicableToHasMultipleValues`() {
        val applicableTo = setOf(
            MetadataUrns.ENTITY_TYPE_TABLE,
            MetadataUrns.ENTITY_TYPE_ATTRIBUTE,
            MetadataUrns.ENTITY_TYPE_SCHEMA
        )
        val descriptor = FacetTypeDescriptor(
            typeKey = "urn:mill/metadata/facet-type:roundtrip-test",
            applicableTo = applicableTo
        )
        repository.save(descriptor)
        val loaded = repository.findByTypeKey("urn:mill/metadata/facet-type:roundtrip-test")
        assertThat(loaded.get().applicableTo).containsExactlyInAnyOrderElementsOf(applicableTo)
    }
}
