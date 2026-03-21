package io.qpointz.mill.persistence.security.jpa.auth

import io.qpointz.mill.persistence.security.jpa.entities.GroupRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserIdentityRecord
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class JpaUserRepoTest {

    @Mock lateinit var identityRepo: UserIdentityRepository
    @Mock lateinit var credentialRepo: UserCredentialRepository
    @Mock lateinit var membershipRepo: GroupMembershipRepository
    @Mock lateinit var userRepo: UserRepository

    private val identity = UserIdentityRecord(
        identityId = "id-1",
        provider = "local",
        subject = "alice",
        userId = "user-1",
        claimsSnapshot = null,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private val credential = UserCredentialRecord(
        credentialId = "cred-1",
        userId = "user-1",
        passwordHash = "{noop}secret",
        algorithm = "noop",
        enabled = true,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun jpaUserRepo() = JpaUserRepo(identityRepo, credentialRepo, membershipRepo, userRepo)

    @Test
    fun `getUsers returns user with correct name and password hash`() {
        whenever(identityRepo.findByProvider("local")).thenReturn(listOf(identity))
        whenever(credentialRepo.findByUserIdAndEnabledTrue("user-1")).thenReturn(credential)
        whenever(membershipRepo.findGroupsByUserId("user-1")).thenReturn(emptyList())

        val users = jpaUserRepo().getUsers()

        assertThat(users).hasSize(1)
        assertThat(users[0].name).isEqualTo("alice")
        assertThat(users[0].password).isEqualTo("{noop}secret")
    }

    @Test
    fun `getUsers includes groups`() {
        val group = GroupRecord(groupId = "g-1", groupName = "admins", description = null)
        whenever(identityRepo.findByProvider("local")).thenReturn(listOf(identity))
        whenever(credentialRepo.findByUserIdAndEnabledTrue("user-1")).thenReturn(credential)
        whenever(membershipRepo.findGroupsByUserId("user-1")).thenReturn(listOf(group))

        val users = jpaUserRepo().getUsers()

        assertThat(users[0].groups).containsExactly("admins")
    }

    @Test
    fun `getUsers excludes user with no enabled credential`() {
        whenever(identityRepo.findByProvider("local")).thenReturn(listOf(identity))
        whenever(credentialRepo.findByUserIdAndEnabledTrue("user-1")).thenReturn(null)

        val users = jpaUserRepo().getUsers()

        assertThat(users).isEmpty()
    }

    @Test
    fun `getUsers returns empty when no local identities`() {
        whenever(identityRepo.findByProvider("local")).thenReturn(emptyList())

        val users = jpaUserRepo().getUsers()

        assertThat(users).isEmpty()
    }
}
