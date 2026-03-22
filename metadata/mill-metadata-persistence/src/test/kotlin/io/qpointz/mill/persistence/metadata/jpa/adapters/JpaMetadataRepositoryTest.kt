package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetScopeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class JpaMetadataRepositoryTest {

    @Mock
    private lateinit var entityRepo: MetadataEntityJpaRepository

    @Mock
    private lateinit var facetScopeRepo: MetadataFacetScopeJpaRepository

    @Mock
    private lateinit var scopeRepo: MetadataScopeJpaRepository

    private lateinit var repository: JpaMetadataRepository

    private val now = Instant.now()

    private val globalScope = MetadataScopeEntity(
        scopeId = MetadataUrns.SCOPE_GLOBAL,
        scopeType = "GLOBAL",
        referenceId = null,
        displayName = "Global",
        ownerId = null,
        visibility = "PUBLIC",
        createdAt = now
    )

    @BeforeEach
    fun setUp() {
        repository = JpaMetadataRepository(entityRepo, facetScopeRepo, scopeRepo)
    }

    // --- toDomain ---

    @Test
    fun `shouldToDomain_whenNoFacetRows`() {
        val record = buildEntityRecord("schema.table")
        val domain = repository.toDomain(record, emptyList(), emptyMap())

        assertThat(domain.id).isEqualTo("schema.table")
        assertThat(domain.type).isEqualTo(MetadataType.TABLE)
        assertThat(domain.schemaName).isEqualTo("mySchema")
        assertThat(domain.tableName).isEqualTo("myTable")
        assertThat(domain.facets).isEmpty()
    }

    @Test
    fun `shouldToDomain_whenFacetRowsPresent`() {
        val record = buildEntityRecord("schema.table")
        val scopeEntity = globalScope
        val facetRow = MetadataFacetScopeEntity(
            entityId = "schema.table",
            facetType = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            scope = scopeEntity,
            payloadJson = """{"title":"My Table"}""",
            createdAt = now,
            updatedAt = now,
            createdBy = null,
            updatedBy = null
        )
        val scopes = mapOf(MetadataUrns.SCOPE_GLOBAL to scopeEntity)

        val domain = repository.toDomain(record, listOf(facetRow), scopes)

        assertThat(domain.facets).containsKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        val scopedFacets = domain.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]!!
        assertThat(scopedFacets).containsKey(MetadataUrns.SCOPE_GLOBAL)
        @Suppress("UNCHECKED_CAST")
        val payload = scopedFacets[MetadataUrns.SCOPE_GLOBAL] as Map<String, Any?>
        assertThat(payload["title"]).isEqualTo("My Table")
    }

    @Test
    fun `shouldToDomain_whenPayloadJsonIsNull`() {
        val record = buildEntityRecord("schema.table")
        val facetRow = MetadataFacetScopeEntity(
            entityId = "schema.table",
            facetType = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            scope = globalScope,
            payloadJson = "null",
            createdAt = now,
            updatedAt = now,
            createdBy = null,
            updatedBy = null
        )

        val domain = repository.toDomain(record, listOf(facetRow), emptyMap())
        val scopedFacets = domain.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]!!
        assertThat(scopedFacets[MetadataUrns.SCOPE_GLOBAL]).isNull()
    }

    @Test
    fun `shouldToDomain_whenEntityTypeIsUnknown`() {
        val record = buildEntityRecord("entity-id", entityType = "UNKNOWN_TYPE")
        val domain = repository.toDomain(record, emptyList(), emptyMap())
        assertThat(domain.type).isNull()
    }

    // --- resolveOrCreateScope ---

    @Test
    fun `shouldResolveScope_whenScopeAlreadyExists`() {
        whenever(scopeRepo.findById(MetadataUrns.SCOPE_GLOBAL)).thenReturn(Optional.of(globalScope))

        val result = repository.resolveOrCreateScope(MetadataUrns.SCOPE_GLOBAL)

        assertThat(result.scopeId).isEqualTo(MetadataUrns.SCOPE_GLOBAL)
        verify(scopeRepo, never()).save(any<MetadataScopeEntity>())
    }

    @Test
    fun `shouldCreateScope_whenScopeAbsent`() {
        val userScopeKey = MetadataUrns.scopeUser("alice")
        whenever(scopeRepo.findById(userScopeKey)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(userScopeKey)

        assertThat(result.scopeId).isEqualTo(userScopeKey)
        assertThat(result.scopeType).isEqualTo("USER")
        assertThat(result.referenceId).isEqualTo("alice")
        verify(scopeRepo).save(any<MetadataScopeEntity>())
    }

    @Test
    fun `shouldCreateTeamScope_whenTeamScopeAbsent`() {
        val teamScope = MetadataUrns.scopeTeam("analysts")
        whenever(scopeRepo.findById(teamScope)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(teamScope)

        assertThat(result.scopeType).isEqualTo("TEAM")
        assertThat(result.referenceId).isEqualTo("analysts")
    }

    @Test
    fun `shouldCreateRoleScope_whenRoleScopeAbsent`() {
        val roleScope = MetadataUrns.scopeRole("admin")
        whenever(scopeRepo.findById(roleScope)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(roleScope)

        assertThat(result.scopeType).isEqualTo("ROLE")
        assertThat(result.referenceId).isEqualTo("admin")
    }

    @Test
    fun `shouldCreateCustomScope_whenUnrecognisedPrefix`() {
        val customScope = "${MetadataUrns.SCOPE_PREFIX}custom:myns"
        whenever(scopeRepo.findById(customScope)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(customScope)

        assertThat(result.scopeType).isEqualTo("CUSTOM")
    }

    // --- findById ---

    @Test
    fun `shouldReturnEmpty_whenEntityNotFound`() {
        whenever(entityRepo.findById("missing")).thenReturn(Optional.empty())
        val result = repository.findById("missing")
        assertThat(result).isEmpty
    }

    @Test
    fun `shouldReturnEntity_whenEntityFound`() {
        val record = buildEntityRecord("schema.table")
        whenever(entityRepo.findById("schema.table")).thenReturn(Optional.of(record))
        whenever(facetScopeRepo.findByEntityId("schema.table")).thenReturn(emptyList())
        whenever(scopeRepo.findAll()).thenReturn(emptyList())

        val result = repository.findById("schema.table")

        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo("schema.table")
    }

    // --- existsById ---

    @Test
    fun `shouldReturnTrue_whenEntityExists`() {
        whenever(entityRepo.existsById("schema.table")).thenReturn(true)
        assertThat(repository.existsById("schema.table")).isTrue()
    }

    @Test
    fun `shouldReturnFalse_whenEntityAbsent`() {
        whenever(entityRepo.existsById("missing")).thenReturn(false)
        assertThat(repository.existsById("missing")).isFalse()
    }

    // --- save (new entity) ---

    @Test
    fun `shouldInsertNewEntity_whenEntityDoesNotExist`() {
        val entity = buildDomainEntity("new.entity")
        whenever(entityRepo.existsById("new.entity")).thenReturn(false)
        whenever(entityRepo.save(any<MetadataEntityRecord>())).thenAnswer { it.arguments[0] as MetadataEntityRecord }

        repository.save(entity)

        verify(entityRepo).save(any<MetadataEntityRecord>())
    }

    @Test
    fun `shouldUpdateExistingEntity_whenEntityAlreadyExists`() {
        val entity = buildDomainEntity("schema.table")
        val record = buildEntityRecord("schema.table")
        whenever(entityRepo.existsById("schema.table")).thenReturn(true)
        whenever(entityRepo.findById("schema.table")).thenReturn(Optional.of(record))
        whenever(entityRepo.save(any<MetadataEntityRecord>())).thenReturn(record)

        repository.save(entity)

        verify(entityRepo).save(record)
    }

    // --- deleteById ---

    @Test
    fun `shouldDeleteById_whenCalled`() {
        repository.deleteById("schema.table")
        verify(entityRepo).deleteById("schema.table")
    }

    // --- deleteAll ---

    @Test
    fun `shouldDeleteAll_whenCalled`() {
        repository.deleteAll()
        verify(entityRepo).deleteAll()
    }

    // --- helpers ---

    private fun buildEntityRecord(
        entityId: String,
        entityType: String = "TABLE"
    ) = MetadataEntityRecord(
        entityId = entityId,
        entityType = entityType,
        schemaName = "mySchema",
        tableName = "myTable",
        attributeName = null,
        createdAt = now,
        updatedAt = now,
        createdBy = null,
        updatedBy = null
    )

    private fun buildDomainEntity(id: String) = MetadataEntity(
        id = id,
        type = MetadataType.TABLE,
        schemaName = "mySchema",
        tableName = "myTable",
        attributeName = null,
        facets = mutableMapOf(),
        createdAt = now,
        updatedAt = now,
        createdBy = null,
        updatedBy = null
    )
}
