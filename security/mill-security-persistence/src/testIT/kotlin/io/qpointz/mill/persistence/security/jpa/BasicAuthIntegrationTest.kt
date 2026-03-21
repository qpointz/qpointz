package io.qpointz.mill.persistence.security.jpa

import io.qpointz.mill.persistence.security.jpa.auth.JpaUserRepo
import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipKey
import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipRecord
import io.qpointz.mill.persistence.security.jpa.entities.GroupRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserIdentityRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserRecord
import io.qpointz.mill.persistence.security.jpa.hasher.NoOpPasswordHasher
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.GroupRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider
import io.qpointz.mill.security.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for the JPA-backed basic-auth path.
 *
 * Verifies end-to-end that [JpaUserRepo] + [UserRepoAuthenticationProvider] correctly
 * authenticate local users, reject bad credentials, and ignore OAuth-only users.
 * All test data is isolated per test via [Transactional] rollback.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class BasicAuthIntegrationTest {

    @Autowired lateinit var userRepo: UserRepository
    @Autowired lateinit var credentialRepo: UserCredentialRepository
    @Autowired lateinit var identityRepo: UserIdentityRepository
    @Autowired lateinit var groupRepo: GroupRepository
    @Autowired lateinit var membershipRepo: GroupMembershipRepository

    private val hasher = NoOpPasswordHasher()
    private val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    private lateinit var aliceUserId: String
    private lateinit var testGroupId: String

    private fun buildProvider(): UserRepoAuthenticationProvider {
        val jpaUserRepo = JpaUserRepo(identityRepo, credentialRepo, membershipRepo, userRepo)
        return UserRepoAuthenticationProvider(jpaUserRepo, encoder)
    }

    private fun authToken(username: String, password: String) =
        UsernamePasswordAuthenticationToken(username, password)

    @BeforeEach
    fun seedDatabase() {
        // Seed local user "alice" with password "secret" and group "testers"
        aliceUserId = UUID.randomUUID().toString()
        userRepo.save(UserRecord(
            userId = aliceUserId,
            status = UserStatus.ACTIVE.name,
            displayName = "Alice",
            primaryEmail = "alice@example.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        ))
        credentialRepo.save(UserCredentialRecord(
            credentialId = UUID.randomUUID().toString(),
            userId = aliceUserId,
            passwordHash = hasher.hash("secret"),
            algorithm = hasher.algorithmId,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        ))
        identityRepo.save(UserIdentityRecord(
            identityId = UUID.randomUUID().toString(),
            provider = "local",
            subject = "alice",
            userId = aliceUserId,
            claimsSnapshot = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        ))
        testGroupId = UUID.randomUUID().toString()
        groupRepo.save(GroupRecord(groupId = testGroupId, groupName = "testers", description = null))
        membershipRepo.save(GroupMembershipRecord(id = GroupMembershipKey(userId = aliceUserId, groupId = testGroupId)))

        // Seed OAuth-only user "bob" — has an entra identity but no UserCredentialRecord
        val bobUserId = UUID.randomUUID().toString()
        userRepo.save(UserRecord(
            userId = bobUserId,
            status = UserStatus.ACTIVE.name,
            displayName = "Bob",
            primaryEmail = "bob@example.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        ))
        identityRepo.save(UserIdentityRecord(
            identityId = UUID.randomUUID().toString(),
            provider = "entra",
            subject = "sub-123",
            userId = bobUserId,
            claimsSnapshot = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        ))
    }

    @Test
    fun `authenticate alice with correct password returns authentication with testers authority`() {
        val provider = buildProvider()

        val result = provider.authenticate(authToken("alice", "secret"))

        assertThat(result).isNotNull
        assertThat(result.isAuthenticated).isTrue()
        val authorityNames = result.authorities.map { it.authority }
        assertThat(authorityNames).contains("testers")
    }

    @Test
    fun `authenticate alice with wrong password returns null`() {
        val provider = buildProvider()

        val result = provider.authenticate(authToken("alice", "wrongpassword"))

        assertThat(result).isNull()
    }

    @Test
    fun `authenticate unknown username returns null`() {
        val provider = buildProvider()

        val result = provider.authenticate(authToken("unknown", "anypassword"))

        assertThat(result).isNull()
    }

    @Test
    fun `authenticate OAuth-only user via basic auth returns null`() {
        val provider = buildProvider()

        // "sub-123" has an entra identity but no local identity or credential
        val result = provider.authenticate(authToken("sub-123", "anypassword"))

        assertThat(result).isNull()
    }

    @Test
    fun `authenticate user with disabled credential returns null`() {
        // Disable alice's credential
        val cred = credentialRepo.findByUserIdAndEnabledTrue(aliceUserId)!!
        cred.enabled = false
        credentialRepo.save(cred)

        val provider = buildProvider()

        val result = provider.authenticate(authToken("alice", "secret"))

        assertThat(result).isNull()
    }
}
