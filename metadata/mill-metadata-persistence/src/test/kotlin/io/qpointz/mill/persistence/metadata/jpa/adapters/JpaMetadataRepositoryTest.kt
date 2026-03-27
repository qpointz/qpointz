package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeInstEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeInstJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
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
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JpaMetadataRepositoryTest {

    @Mock
    private lateinit var entityRepo: MetadataEntityJpaRepository

    @Mock
    private lateinit var facetRepo: MetadataFacetJpaRepository

    @Mock
    private lateinit var facetTypeInstRepo: MetadataFacetTypeInstJpaRepository

    @Mock
    private lateinit var facetTypeDefRepo: MetadataFacetTypeJpaRepository

    @Mock
    private lateinit var scopeRepo: MetadataScopeJpaRepository

    private lateinit var repository: JpaMetadataRepository

    private val now = Instant.now()

    private val globalScope = MetadataScopeEntity(
        scopeId = 1L,
        scopeRes = MetadataUrns.SCOPE_GLOBAL,
        scopeType = "GLOBAL",
        referenceId = null,
        displayName = "Global",
        ownerId = null,
        visibility = "PUBLIC",
        createdAt = now
    )

    @BeforeEach
    fun setUp() {
        repository = JpaMetadataRepository(entityRepo, facetRepo, facetTypeInstRepo, facetTypeDefRepo, scopeRepo)
    }

    @Test
    fun `shouldToDomain_whenNoFacetRows`() {
        val record = buildEntityRecord("schema.table")
        val domain = repository.toDomain(record, emptyList())

        assertThat(domain.id).isEqualTo("schema.table")
        assertThat(domain.type).isEqualTo(MetadataType.TABLE)
        assertThat(domain.schemaName).isEqualTo("mySchema")
        assertThat(domain.tableName).isEqualTo("myTable")
        assertThat(domain.facets).isEmpty()
    }

    @Test
    fun `shouldToDomain_whenFacetRowsPresent`() {
        val record = buildEntityRecord("schema.table")
        val facetType = instFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        val facetRow = MetadataFacetEntity(
            facetId = 1L,
            entity = record,
            scope = globalScope,
            facetType = facetType,
            payloadJson = """{"title":"My Table"}""",
            facetUid = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            createdBy = null,
            updatedBy = null
        )

        val domain = repository.toDomain(record, listOf(facetRow))

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
        val facetRow = MetadataFacetEntity(
            facetId = 1L,
            entity = record,
            scope = globalScope,
            facetType = instFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE),
            payloadJson = "null",
            facetUid = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            createdBy = null,
            updatedBy = null
        )

        val domain = repository.toDomain(record, listOf(facetRow))
        val scopedFacets = domain.facets[MetadataUrns.FACET_TYPE_DESCRIPTIVE]!!
        assertThat(scopedFacets[MetadataUrns.SCOPE_GLOBAL]).isNull()
    }

    @Test
    fun `shouldToDomain_whenEntityTypeIsUnknown`() {
        val record = buildEntityRecord("entity-id", entityType = "UNKNOWN_TYPE")
        val domain = repository.toDomain(record, emptyList())
        assertThat(domain.type).isNull()
    }

    @Test
    fun `shouldResolveScope_whenScopeAlreadyExists`() {
        whenever(scopeRepo.findByScopeRes(MetadataUrns.SCOPE_GLOBAL)).thenReturn(Optional.of(globalScope))

        val result = repository.resolveOrCreateScope(MetadataUrns.SCOPE_GLOBAL)

        assertThat(result.scopeRes).isEqualTo(MetadataUrns.SCOPE_GLOBAL)
        verify(scopeRepo, never()).save(any<MetadataScopeEntity>())
    }

    @Test
    fun `shouldCreateScope_whenScopeAbsent`() {
        val userScopeKey = MetadataUrns.scopeUser("alice")
        whenever(scopeRepo.findByScopeRes(userScopeKey)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(userScopeKey)

        assertThat(result.scopeRes).isEqualTo(userScopeKey)
        assertThat(result.scopeType).isEqualTo("USER")
        assertThat(result.referenceId).isEqualTo("alice")
        verify(scopeRepo).save(any<MetadataScopeEntity>())
    }

    @Test
    fun `shouldCreateTeamScope_whenTeamScopeAbsent`() {
        val teamScope = MetadataUrns.scopeTeam("analysts")
        whenever(scopeRepo.findByScopeRes(teamScope)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(teamScope)

        assertThat(result.scopeType).isEqualTo("TEAM")
        assertThat(result.referenceId).isEqualTo("analysts")
    }

    @Test
    fun `shouldCreateRoleScope_whenRoleScopeAbsent`() {
        val roleScope = MetadataUrns.scopeRole("admin")
        whenever(scopeRepo.findByScopeRes(roleScope)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(roleScope)

        assertThat(result.scopeType).isEqualTo("ROLE")
        assertThat(result.referenceId).isEqualTo("admin")
    }

    @Test
    fun `shouldCreateCustomScope_whenUnrecognisedPrefix`() {
        val customScope = "${MetadataUrns.SCOPE_PREFIX}custom:myns"
        whenever(scopeRepo.findByScopeRes(customScope)).thenReturn(Optional.empty())
        whenever(scopeRepo.save(any<MetadataScopeEntity>())).thenAnswer { it.arguments[0] as MetadataScopeEntity }

        val result = repository.resolveOrCreateScope(customScope)

        assertThat(result.scopeType).isEqualTo("CUSTOM")
    }

    @Test
    fun `shouldReturnEmpty_whenEntityNotFound`() {
        whenever(entityRepo.findByEntityRes("missing")).thenReturn(Optional.empty())
        val result = repository.findById("missing")
        assertThat(result).isEmpty
    }

    @Test
    fun `shouldReturnEntity_whenEntityFound`() {
        val record = buildEntityRecord("schema.table")
        whenever(entityRepo.findByEntityRes("schema.table")).thenReturn(Optional.of(record))
        whenever(facetRepo.findByEntityEntityRes("schema.table")).thenReturn(emptyList())

        val result = repository.findById("schema.table")

        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo("schema.table")
    }

    @Test
    fun `shouldReturnTrue_whenEntityExists`() {
        whenever(entityRepo.existsByEntityRes("schema.table")).thenReturn(true)
        assertThat(repository.existsById("schema.table")).isTrue()
    }

    @Test
    fun `shouldReturnFalse_whenEntityAbsent`() {
        whenever(entityRepo.existsByEntityRes("missing")).thenReturn(false)
        assertThat(repository.existsById("missing")).isFalse()
    }

    @Test
    fun `shouldInsertNewEntity_whenEntityDoesNotExist`() {
        val entity = buildDomainEntity("new.entity")
        val saved = buildEntityRecord("new.entity").let { r ->
            MetadataEntityRecord(
                entityId = 42L,
                entityRes = r.entityRes,
                entityType = r.entityType,
                schemaName = r.schemaName,
                tableName = r.tableName,
                attributeName = r.attributeName,
                createdAt = r.createdAt,
                updatedAt = r.updatedAt,
                createdBy = r.createdBy,
                updatedBy = r.updatedBy
            )
        }
        whenever(entityRepo.existsByEntityRes("new.entity")).thenReturn(false)
        whenever(entityRepo.save(any<MetadataEntityRecord>())).thenReturn(saved)
        whenever(entityRepo.findById(42L)).thenReturn(Optional.of(saved))

        repository.save(entity)

        verify(entityRepo).save(any<MetadataEntityRecord>())
    }

    @Test
    fun `shouldUpdateExistingEntity_whenEntityAlreadyExists`() {
        val entity = buildDomainEntity("schema.table")
        val record = buildEntityRecord("schema.table")
        whenever(entityRepo.existsByEntityRes("schema.table")).thenReturn(true)
        whenever(entityRepo.findByEntityRes("schema.table")).thenReturn(Optional.of(record))
        whenever(entityRepo.save(any<MetadataEntityRecord>())).thenReturn(record)
        whenever(entityRepo.findById(record.entityId)).thenReturn(Optional.of(record))

        repository.save(entity)

        verify(entityRepo).save(record)
    }

    @Test
    fun `shouldDeleteById_whenCalled`() {
        repository.deleteById("schema.table")
        verify(entityRepo).deleteByEntityRes("schema.table")
    }

    @Test
    fun `shouldDeleteAll_whenCalled`() {
        repository.deleteAll()
        verify(entityRepo).deleteAll()
    }

    private fun instFacet(typeRes: String) = MetadataFacetTypeInstEntity(
        facetTypeId = 99L,
        typeRes = typeRes,
        slug = typeRes.substringAfterLast(':'),
        displayName = null,
        description = null,
        source = "DEFINED",
        facetTypeDef = null,
        createdAt = now,
        updatedAt = now,
        createdBy = null,
        updatedBy = null
    )

    private fun buildEntityRecord(
        entityRes: String,
        entityType: String = "TABLE"
    ) = MetadataEntityRecord(
        entityId = 1L,
        entityRes = entityRes,
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
