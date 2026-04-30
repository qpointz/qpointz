package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaMetadataScopeRepositoryIT {

    @Autowired
    private lateinit var repository: MetadataScopeRepository

    private fun now() = Instant.now()

    @Test
    fun `should find global scope when V4 migration ran`() {
        val found = repository.findByRes(MetadataUrns.SCOPE_GLOBAL)
        assertThat(found).isNotNull
        assertThat(found!!.res).isEqualTo(MetadataUrns.SCOPE_GLOBAL)
        assertThat(found.displayName).isEqualTo("Global")
    }

    @Test
    fun `should return true when global scope exists`() {
        assertThat(repository.exists(MetadataUrns.SCOPE_GLOBAL)).isTrue()
    }

    @Test
    fun `should return false when scope absent`() {
        assertThat(
            repository.exists("urn:mill/metadata/scope:user:nonexistent-${java.util.UUID.randomUUID()}")
        ).isFalse()
    }

    @Test
    fun `should persist and find user scope when saved`() {
        val res = MetadataUrns.scopeUser("testuser-${java.util.UUID.randomUUID().toString().take(8)}")
        val t = now()
        repository.save(
            MetadataScope(
                res = res,
                scopeType = "USER",
                referenceId = res.removePrefix(MetadataUrns.SCOPE_PREFIX).removePrefix("user:"),
                displayName = "Test User",
                ownerId = "testuser",
                visibility = "PUBLIC",
                uuid = null,
                createdAt = t,
                createdBy = null,
                lastModifiedAt = t,
                lastModifiedBy = null
            )
        )

        val found = repository.findByRes(res)
        assertThat(found).isNotNull
        assertThat(found!!.displayName).isEqualTo("Test User")
        assertThat(found.ownerId).isEqualTo("testuser")
    }

    @Test
    fun `should delete when scope exists`() {
        val res = MetadataUrns.scopeUser("del-${java.util.UUID.randomUUID().toString().take(8)}")
        val t = now()
        repository.save(
            MetadataScope(
                res = res,
                scopeType = "USER",
                referenceId = "del",
                displayName = null,
                ownerId = null,
                visibility = "PUBLIC",
                uuid = null,
                createdAt = t,
                createdBy = null,
                lastModifiedAt = t,
                lastModifiedBy = null
            )
        )
        assertThat(repository.exists(res)).isTrue()

        repository.delete(res)

        assertThat(repository.exists(res)).isFalse()
    }
}
