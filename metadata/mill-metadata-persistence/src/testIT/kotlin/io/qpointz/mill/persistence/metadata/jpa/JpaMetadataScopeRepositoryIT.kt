package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataScopeRepository
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
class JpaMetadataScopeRepositoryIT {

    @Autowired
    private lateinit var jpaRepo: MetadataScopeJpaRepository

    private val repository by lazy { JpaMetadataScopeRepository(jpaRepo) }

    @Test
    fun `shouldFindGlobalScope_whenV4MigrationRan`() {
        val found = repository.findById(MetadataUrns.SCOPE_GLOBAL)
        assertThat(found).isPresent
        assertThat(found.get().scopeId).isEqualTo(MetadataUrns.SCOPE_GLOBAL)
        assertThat(found.get().displayName).isEqualTo("Global")
    }

    @Test
    fun `shouldReturnTrue_whenGlobalScopeExists`() {
        assertThat(repository.existsById(MetadataUrns.SCOPE_GLOBAL)).isTrue()
    }

    @Test
    fun `shouldReturnFalse_whenScopeAbsent`() {
        assertThat(repository.existsById("urn:mill/metadata/scope:user:nonexistent-${UUID.randomUUID()}")).isFalse()
    }

    @Test
    fun `shouldPersistAndFindUserScope_whenSaved`() {
        val scopeId = MetadataUrns.scopeUser("testuser-${UUID.randomUUID().toString().take(8)}")
        val scope = MetadataScope(scopeId = scopeId, displayName = "Test User", ownerId = "testuser", createdAt = Instant.now())

        repository.save(scope)

        val found = repository.findById(scopeId)
        assertThat(found).isPresent
        assertThat(found.get().displayName).isEqualTo("Test User")
        assertThat(found.get().ownerId).isEqualTo("testuser")
    }

    @Test
    fun `shouldPersistAndFindTeamScope_whenSaved`() {
        val scopeId = MetadataUrns.scopeTeam("team-${UUID.randomUUID().toString().take(8)}")
        val scope = MetadataScope(scopeId = scopeId, displayName = "Engineering", ownerId = null, createdAt = Instant.now())

        repository.save(scope)

        val found = repository.findById(scopeId)
        assertThat(found).isPresent
        assertThat(found.get().scopeId).isEqualTo(scopeId)
    }

    @Test
    fun `shouldFindAll_whenMultipleScopesExist`() {
        val id1 = MetadataUrns.scopeUser("user-${UUID.randomUUID().toString().take(8)}")
        val id2 = MetadataUrns.scopeTeam("team-${UUID.randomUUID().toString().take(8)}")
        repository.save(MetadataScope(id1, null, null, Instant.now()))
        repository.save(MetadataScope(id2, null, null, Instant.now()))

        val all = repository.findAll()
        val ids = all.map { it.scopeId }
        assertThat(ids).contains(MetadataUrns.SCOPE_GLOBAL, id1, id2)
    }

    @Test
    fun `shouldDelete_whenScopeExists`() {
        val scopeId = MetadataUrns.scopeUser("del-${UUID.randomUUID().toString().take(8)}")
        repository.save(MetadataScope(scopeId, null, null, Instant.now()))
        assertThat(repository.existsById(scopeId)).isTrue()

        repository.deleteById(scopeId)

        assertThat(repository.existsById(scopeId)).isFalse()
    }
}
