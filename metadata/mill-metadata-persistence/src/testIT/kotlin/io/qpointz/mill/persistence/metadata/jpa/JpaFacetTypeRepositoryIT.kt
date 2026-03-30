package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.FacetTypeSource
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaFacetTypeRepositoryIT {

    @Autowired
    private lateinit var facetTypeRepository: FacetTypeRepository

    @Autowired
    private lateinit var facetTypeDefinitionRepository: FacetTypeDefinitionRepository

    private fun now() = Instant.now()

    private fun facetType(key: String, def: FacetTypeDefinition): FacetType {
        val t = now()
        return FacetType(
            typeKey = key,
            source = FacetTypeSource.DEFINED,
            definition = def,
            createdAt = t,
            createdBy = null,
            lastModifiedAt = t,
            lastModifiedBy = null
        )
    }

    private fun definition(
        typeKey: String,
        displayName: String? = null,
        description: String? = null,
        category: String? = null,
        mandatory: Boolean = false,
        enabled: Boolean = true,
        applicableTo: List<String>? = null,
        contentSchema: Map<String, Any?>? = null
    ): FacetTypeDefinition {
        val t = now()
        return FacetTypeDefinition(
            typeKey = typeKey,
            displayName = displayName,
            description = description,
            category = category,
            mandatory = mandatory,
            enabled = enabled,
            targetCardinality = FacetTargetCardinality.SINGLE,
            applicableTo = applicableTo,
            contentSchema = contentSchema,
            schemaVersion = "1.0",
            createdAt = t,
            createdBy = null,
            lastModifiedAt = t,
            lastModifiedBy = null
        )
    }

    @Test
    fun `should find platform bootstrap facet types after seed ran`() {
        val keys = facetTypeRepository.findAll().map { it.typeKey }.toSet()
        assertThat(keys).contains(
            MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            MetadataUrns.FACET_TYPE_RELATION
        )
    }

    @Test
    fun `should persist and retrieve when new defined facet type is saved`() {
        val key = "urn:mill/metadata/facet-type:governance-${UUID.randomUUID().toString().take(8)}"
        val def = definition(
            typeKey = key,
            displayName = "Governance",
            description = "Data governance annotations",
            applicableTo = listOf(
                "urn:mill/metadata/entity-type:table",
                "urn:mill/metadata/entity-type:schema"
            )
        )
        facetTypeRepository.save(facetType(key, def))

        val found = facetTypeRepository.findByKey(key)
        assertThat(found).isNotNull
        assertThat(found!!.definition!!.displayName).isEqualTo("Governance")
        assertThat(found.definition!!.applicableTo).containsExactlyInAnyOrder(
            "urn:mill/metadata/entity-type:table",
            "urn:mill/metadata/entity-type:schema"
        )
    }

    @Test
    fun `should round trip category on facet type definition`() {
        val key = "urn:mill/metadata/facet-type:category-${UUID.randomUUID().toString().take(8)}"
        val def = definition(typeKey = key, displayName = "Cat test", category = "relation")
        facetTypeRepository.save(facetType(key, def))

        val loaded = facetTypeRepository.findByKey(key)
        assertThat(loaded).isNotNull
        assertThat(loaded!!.definition!!.category).isEqualTo("relation")
        assertThat(facetTypeDefinitionRepository.findByKey(key)!!.category).isEqualTo("relation")
    }

    @Test
    fun `should round trip content schema when definition includes schema`() {
        val key = "urn:mill/metadata/facet-type:schema-test-${UUID.randomUUID().toString().take(8)}"
        val schema = mapOf<String, Any?>(
            "type" to "object",
            "properties" to mapOf("title" to mapOf("type" to "string"))
        )
        val def = definition(typeKey = key, contentSchema = schema)
        facetTypeRepository.save(facetType(key, def))

        val found = facetTypeRepository.findByKey(key)
        assertThat(found).isNotNull
        assertThat(found!!.definition!!.contentSchema).isNotNull
        assertThat(found.definition!!.contentSchema!!["type"]).isEqualTo("object")
    }

    @Test
    fun `should return null when type key absent`() {
        assertThat(facetTypeRepository.findByKey("urn:mill/metadata/facet-type:does-not-exist-xyz"))
            .isNull()
    }

    @Test
    fun `should find descriptive when seeded`() {
        assertThat(facetTypeRepository.findByKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)).isNotNull
    }

    @Test
    fun `should not find when facet type absent`() {
        assertThat(facetTypeRepository.findByKey("urn:mill/metadata/facet-type:absent-${UUID.randomUUID()}"))
            .isNull()
    }

    @Test
    fun `should delete runtime row when delete called`() {
        val key = "urn:mill/metadata/facet-type:to-delete-${UUID.randomUUID().toString().take(8)}"
        facetTypeRepository.save(facetType(key, definition(typeKey = key)))
        assertThat(facetTypeRepository.findByKey(key)).isNotNull

        facetTypeRepository.delete(key)

        assertThat(facetTypeRepository.findByKey(key)).isNull()
    }

    @Test
    fun `should round trip when applicableTo has multiple values`() {
        val key = "urn:mill/metadata/facet-type:roundtrip-${UUID.randomUUID().toString().take(8)}"
        val applicable = listOf(
            "urn:mill/metadata/entity-type:table",
            "urn:mill/metadata/entity-type:attribute",
            "urn:mill/metadata/entity-type:schema"
        )
        facetTypeRepository.save(facetType(key, definition(typeKey = key, applicableTo = applicable)))

        val loaded = facetTypeRepository.findByKey(key)
        assertThat(loaded!!.definition!!.applicableTo).containsExactlyInAnyOrderElementsOf(applicable)
    }

    @Test
    fun `should expose definition rows via definition repository`() {
        val def = facetTypeDefinitionRepository.findByKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        assertThat(def).isNotNull
        assertThat(def!!.typeKey).isEqualTo(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
    }
}
