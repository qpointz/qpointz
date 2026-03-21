package io.qpointz.mill.persistence.security.jpa.service

import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipKey
import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipRecord
import io.qpointz.mill.persistence.security.jpa.entities.GroupRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserIdentityRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserProfileRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserRecord
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserProfileRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.security.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class JpaUserIdentityResolutionServiceTest {

    @Mock lateinit var userRepo: UserRepository
    @Mock lateinit var identityRepo: UserIdentityRepository
    @Mock lateinit var profileRepo: UserProfileRepository
    @Mock lateinit var membershipRepo: GroupMembershipRepository

    private val service by lazy {
        JpaUserIdentityResolutionService(userRepo, identityRepo, profileRepo, membershipRepo)
    }

    // --- helpers -----------------------------------------------------------

    private fun userRecord(
        userId: String = "user-1",
        status: String = UserStatus.ACTIVE.name,
        displayName: String? = "Alice",
        primaryEmail: String? = "alice@example.com",
    ) = UserRecord(
        userId = userId,
        status = status,
        displayName = displayName,
        primaryEmail = primaryEmail,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun identityRecord(userId: String = "user-1", provider: String = "local", subject: String = "alice") =
        UserIdentityRecord(
            identityId = "identity-1",
            provider = provider,
            subject = subject,
            userId = userId,
            claimsSnapshot = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    private fun groupRecord(groupId: String, groupName: String) =
        GroupRecord(groupId = groupId, groupName = groupName, description = null)

    // --- resolve() ---------------------------------------------------------

    @Test
    fun `resolve - found case returns ResolvedUser with correct fields`() {
        val user = userRecord()
        val identity = identityRecord()
        whenever(identityRepo.findByProviderAndSubject("local", "alice")).thenReturn(identity)
        whenever(userRepo.findById("user-1")).thenReturn(Optional.of(user))

        val result = service.resolve("local", "alice")

        assertThat(result).isNotNull
        assertThat(result!!.userId).isEqualTo("user-1")
        assertThat(result.displayName).isEqualTo("Alice")
        assertThat(result.primaryEmail).isEqualTo("alice@example.com")
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
    }

    @Test
    fun `resolve - identity missing returns null`() {
        whenever(identityRepo.findByProviderAndSubject("local", "unknown")).thenReturn(null)

        val result = service.resolve("local", "unknown")

        assertThat(result).isNull()
        verify(userRepo, never()).findById(any())
    }

    @Test
    fun `resolve - identity exists but user record missing returns null`() {
        val identity = identityRecord(userId = "ghost-user")
        whenever(identityRepo.findByProviderAndSubject("local", "alice")).thenReturn(identity)
        whenever(userRepo.findById("ghost-user")).thenReturn(Optional.empty())

        val result = service.resolve("local", "alice")

        assertThat(result).isNull()
    }

    // --- resolveOrProvision() ----------------------------------------------

    @Test
    fun `resolveOrProvision - existing identity returns same userId without creating new records`() {
        val user = userRecord()
        val identity = identityRecord()
        whenever(identityRepo.findByProviderAndSubject("local", "alice")).thenReturn(identity)
        whenever(userRepo.findById("user-1")).thenReturn(Optional.of(user))

        val result = service.resolveOrProvision("local", "alice")

        assertThat(result.userId).isEqualTo("user-1")
        verify(userRepo, never()).save(any())
        verify(identityRepo, never()).save(any())
        verify(profileRepo, never()).save(any())
    }

    @Test
    fun `resolveOrProvision - new identity creates UserRecord, UserIdentityRecord, UserProfileRecord and returns ACTIVE ResolvedUser`() {
        whenever(identityRepo.findByProviderAndSubject("local", "newuser")).thenReturn(null)

        val userCaptor = ArgumentCaptor.forClass(UserRecord::class.java)
        val identityCaptor = ArgumentCaptor.forClass(UserIdentityRecord::class.java)
        val profileCaptor = ArgumentCaptor.forClass(UserProfileRecord::class.java)

        whenever(userRepo.save(any<UserRecord>())).thenAnswer { it.arguments[0] as UserRecord }
        whenever(identityRepo.save(any<UserIdentityRecord>())).thenAnswer { it.arguments[0] as UserIdentityRecord }
        whenever(profileRepo.save(any<UserProfileRecord>())).thenAnswer { it.arguments[0] as UserProfileRecord }

        val result = service.resolveOrProvision("local", "newuser", "New User", "new@example.com")

        verify(userRepo).save(userCaptor.capture())
        verify(identityRepo).save(identityCaptor.capture())
        verify(profileRepo).save(profileCaptor.capture())

        // ResolvedUser assertions
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(result.displayName).isEqualTo("New User")
        assertThat(result.primaryEmail).isEqualTo("new@example.com")
        assertThat(result.userId).isNotBlank()

        // UserRecord saved with correct fields
        val savedUser = userCaptor.value
        assertThat(savedUser.status).isEqualTo(UserStatus.ACTIVE.name)
        assertThat(savedUser.displayName).isEqualTo("New User")
        assertThat(savedUser.primaryEmail).isEqualTo("new@example.com")

        // UserIdentityRecord saved with correct provider/subject
        val savedIdentity = identityCaptor.value
        assertThat(savedIdentity.provider).isEqualTo("local")
        assertThat(savedIdentity.subject).isEqualTo("newuser")
        assertThat(savedIdentity.userId).isEqualTo(savedUser.userId)

        // UserProfileRecord saved with correct userId
        val savedProfile = profileCaptor.value
        assertThat(savedProfile.userId).isEqualTo(savedUser.userId)
        assertThat(savedProfile.displayName).isEqualTo("New User")
        assertThat(savedProfile.email).isEqualTo("new@example.com")
    }

    // --- loadAuthorities() -------------------------------------------------

    @Test
    fun `loadAuthorities - returns group names for user`() {
        val groups = listOf(
            groupRecord("g1", "ROLE_ADMIN"),
            groupRecord("g2", "ROLE_USER"),
        )
        whenever(membershipRepo.findGroupsByUserId("user-1")).thenReturn(groups)

        val authorities = service.loadAuthorities("user-1")

        assertThat(authorities).containsExactly("ROLE_ADMIN", "ROLE_USER")
    }

    @Test
    fun `loadAuthorities - returns empty list when user has no groups`() {
        whenever(membershipRepo.findGroupsByUserId("user-no-groups")).thenReturn(emptyList())

        val authorities = service.loadAuthorities("user-no-groups")

        assertThat(authorities).isEmpty()
    }
}
