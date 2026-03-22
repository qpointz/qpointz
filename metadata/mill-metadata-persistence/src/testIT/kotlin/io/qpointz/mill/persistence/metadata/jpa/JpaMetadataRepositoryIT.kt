package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetScopeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
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
class JpaMetadataRepositoryIT {

    @Autowired private lateinit var entityRepo: MetadataEntityJpaRepository
    @Autowired private lateinit var facetScopeRepo: MetadataFacetScopeJpaRepository
    @Autowired private lateinit var scopeRepo: MetadataScopeJpaRepository

    private val repository by lazy {
        JpaMetadataRepository(entityRepo, facetScopeRepo, scopeRepo)
    }

    private val now = Instant.now()

    // --- save + findById ---

    @Test
    fun `shouldSaveAndFindById_whenEntityHasNoFacets`() {
        val id = "schema.${UUID.randomUUID()}"
        val entity = buildEntity(id, schemaName = "mySchema")

        repository.save(entity)

        val found = repository.findById(id)
        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(id)
        assertThat(found.get().schemaName).isEqualTo("mySchema")
        assertThat(found.get().type).isEqualTo(MetadataType.SCHEMA)
        assertThat(found.get().facets).isEmpty()
    }

    @Test
    fun `shouldSaveAndFindById_whenEntityHasGlobalFacet`() {
        val id = "schema.table.${UUID.randomUUID()}"
        val entity = buildEntity(id, schemaName = "s", tableName = "t", type = MetadataType.TABLE)
        entity.setFacet(
            MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            MetadataUrns.SCOPE_GLOBAL,
            mapOf("title" to "My Table")
        )

        repository.save(entity)

        val found = repository.findById(id).get()
        assertThat(found.facets).containsKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        val scopedFacets = found.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]!!
        assertThat(scopedFacets).containsKey(MetadataUrns.SCOPE_GLOBAL)
        @Suppress("UNCHECKED_CAST")
        val payload = scopedFacets[MetadataUrns.SCOPE_GLOBAL] as Map<String, Any?>
        assertThat(payload["title"]).isEqualTo("My Table")
    }

    @Test
    fun `shouldSaveAndFindById_whenEntityHasUserScopedFacet`() {
        val id = "schema.table.user.${UUID.randomUUID()}"
        val userScope = MetadataUrns.scopeUser("alice")
        val entity = buildEntity(id, schemaName = "s", tableName = "t", type = MetadataType.TABLE)
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, userScope, mapOf("note" to "user note"))

        repository.save(entity)

        val found = repository.findById(id).get()
        val scopedFacets = found.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]!!
        assertThat(scopedFacets).containsKey(userScope)
    }

    @Test
    fun `shouldReturnEmpty_whenEntityNotFound`() {
        val result = repository.findById("does.not.exist.${UUID.randomUUID()}")
        assertThat(result).isEmpty
    }

    // --- update ---

    @Test
    fun `shouldUpdateEntity_whenSavedTwiceWithSameId`() {
        val id = "schema.update.${UUID.randomUUID()}"
        val entity = buildEntity(id, schemaName = "original")
        repository.save(entity)

        entity.schemaName = "updated"
        repository.save(entity)

        val found = repository.findById(id).get()
        assertThat(found.schemaName).isEqualTo("updated")
    }

    @Test
    fun `shouldUpdateFacetPayload_whenFacetSavedTwice`() {
        val id = "schema.facet-update.${UUID.randomUUID()}"
        val entity = buildEntity(id, schemaName = "s")
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL, mapOf("title" to "Original"))
        repository.save(entity)

        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL, mapOf("title" to "Updated"))
        repository.save(entity)

        val found = repository.findById(id).get()
        @Suppress("UNCHECKED_CAST")
        val payload = found.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]!![MetadataUrns.SCOPE_GLOBAL] as Map<String, Any?>
        assertThat(payload["title"]).isEqualTo("Updated")
    }

    // --- findByLocation ---

    @Test
    fun `shouldFindByLocation_whenSchemaTableAttributeMatch`() {
        val id = "location.test.${UUID.randomUUID()}"
        val uniqueSchema = "loc_schema_${UUID.randomUUID().toString().take(8)}"
        val entity = buildEntity(id, schemaName = uniqueSchema, tableName = "t", type = MetadataType.TABLE)
        repository.save(entity)

        val found = repository.findByLocation(uniqueSchema, "t", null)
        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(id)
    }

    @Test
    fun `shouldReturnEmpty_whenLocationNotMatched`() {
        val result = repository.findByLocation("nonexistent_${UUID.randomUUID()}", null, null)
        assertThat(result).isEmpty
    }

    // --- findByType ---

    @Test
    fun `shouldFindByType_whenEntitiesOfTypeExist`() {
        val id1 = "type.schema1.${UUID.randomUUID()}"
        val id2 = "type.schema2.${UUID.randomUUID()}"
        repository.save(buildEntity(id1, schemaName = id1, type = MetadataType.SCHEMA))
        repository.save(buildEntity(id2, schemaName = id2, type = MetadataType.SCHEMA))

        val found = repository.findByType(MetadataType.SCHEMA)
        val ids = found.map { it.id }
        assertThat(ids).contains(id1, id2)
    }

    // --- existsById ---

    @Test
    fun `shouldReturnTrue_whenEntityExists`() {
        val id = "exists.${UUID.randomUUID()}"
        repository.save(buildEntity(id, schemaName = id))
        assertThat(repository.existsById(id)).isTrue()
    }

    @Test
    fun `shouldReturnFalse_whenEntityAbsent`() {
        assertThat(repository.existsById("absent.${UUID.randomUUID()}")).isFalse()
    }

    // --- deleteById ---

    @Test
    fun `shouldDeleteById_whenEntityExists`() {
        val id = "delete.${UUID.randomUUID()}"
        repository.save(buildEntity(id, schemaName = id))
        assertThat(repository.existsById(id)).isTrue()

        repository.deleteById(id)

        assertThat(repository.existsById(id)).isFalse()
    }

    // --- findAll ---

    @Test
    fun `shouldFindAll_whenEntitiesArePresent`() {
        val id1 = "all.a.${UUID.randomUUID()}"
        val id2 = "all.b.${UUID.randomUUID()}"
        repository.save(buildEntity(id1, schemaName = id1))
        repository.save(buildEntity(id2, schemaName = id2))

        val all = repository.findAll()
        val ids = all.map { it.id }
        assertThat(ids).contains(id1, id2)
    }

    // --- scope creation via facet save ---

    @Test
    fun `shouldCreateGlobalScopedFacet_whenGlobalScopeIsSeeded`() {
        val id = "scope.global.${UUID.randomUUID()}"
        val entity = buildEntity(id, schemaName = id)
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL, mapOf("x" to 1))
        repository.save(entity)
        val found = repository.findById(id).get()
        assertThat(found.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]).containsKey(MetadataUrns.SCOPE_GLOBAL)
    }

    @Test
    fun `shouldCreateNewUserScopedFacet_whenUserScopeIsAbsent`() {
        val id = "scope.user.${UUID.randomUUID()}"
        val userScope = MetadataUrns.scopeUser("testuser-${UUID.randomUUID().toString().take(8)}")
        val entity = buildEntity(id, schemaName = id)
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, userScope, mapOf("note" to "created"))
        repository.save(entity)
        val found = repository.findById(id).get()
        assertThat(found.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]).containsKey(userScope)
    }

    // --- helpers ---

    private fun buildEntity(
        id: String,
        schemaName: String? = null,
        tableName: String? = null,
        attributeName: String? = null,
        type: MetadataType = MetadataType.SCHEMA
    ) = MetadataEntity(
        id = id,
        type = type,
        schemaName = schemaName,
        tableName = tableName,
        attributeName = attributeName,
        facets = mutableMapOf(),
        createdAt = now,
        updatedAt = now,
        createdBy = "test",
        updatedBy = "test"
    )
}
