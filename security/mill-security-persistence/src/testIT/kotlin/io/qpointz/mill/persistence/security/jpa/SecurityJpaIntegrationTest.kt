package io.qpointz.mill.persistence.security.jpa

import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipKey
import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipRecord
import io.qpointz.mill.persistence.security.jpa.entities.GroupRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserIdentityRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserProfileRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserRecord
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.GroupRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserProfileRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.persistence.security.jpa.service.JpaUserIdentityResolutionService
import io.qpointz.mill.security.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@Transactional
class SecurityJpaIntegrationTest {

    @Autowired lateinit var userRepo: UserRepository
    @Autowired lateinit var credentialRepo: UserCredentialRepository
    @Autowired lateinit var identityRepo: UserIdentityRepository
    @Autowired lateinit var groupRepo: GroupRepository
    @Autowired lateinit var membershipRepo: GroupMembershipRepository
    @Autowired lateinit var profileRepo: UserProfileRepository

    private val service by lazy {
        JpaUserIdentityResolutionService(userRepo, identityRepo, profileRepo, membershipRepo)
    }

    // --- Entity persistence roundtrip tests --------------------------------

    @Test
    fun `UserRecord can be saved and retrieved`() {
        val userId = UUID.randomUUID().toString()
        val user = UserRecord(
            userId = userId,
            status = UserStatus.ACTIVE.name,
            displayName = "Alice",
            primaryEmail = "alice@example.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        userRepo.save(user)
        val loaded = userRepo.findById(userId)
        assertThat(loaded).isPresent
        assertThat(loaded.get().displayName).isEqualTo("Alice")
        assertThat(loaded.get().primaryEmail).isEqualTo("alice@example.com")
        assertThat(loaded.get().status).isEqualTo(UserStatus.ACTIVE.name)
    }

    @Test
    fun `UserCredentialRecord can be saved and retrieved`() {
        val userId = UUID.randomUUID().toString()
        userRepo.save(UserRecord(userId = userId, status = UserStatus.ACTIVE.name, displayName = null,
            primaryEmail = null, createdAt = Instant.now(), updatedAt = Instant.now()))

        val credentialId = UUID.randomUUID().toString()
        credentialRepo.save(UserCredentialRecord(
            credentialId = credentialId,
            userId = userId,
            passwordHash = "{bcrypt}hashed",
            algorithm = "bcrypt",
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        ))

        val loaded = credentialRepo.findByUserIdAndEnabledTrue(userId)
        assertThat(loaded).isNotNull
        assertThat(loaded!!.algorithm).isEqualTo("bcrypt")
        assertThat(loaded.enabled).isTrue()
    }

    @Test
    fun `UserIdentityRecord can be saved and retrieved`() {
        val userId = UUID.randomUUID().toString()
        userRepo.save(UserRecord(userId = userId, status = UserStatus.ACTIVE.name, displayName = null,
            primaryEmail = null, createdAt = Instant.now(), updatedAt = Instant.now()))

        val identityId = UUID.randomUUID().toString()
        identityRepo.save(UserIdentityRecord(
            identityId = identityId,
            provider = "local",
            subject = "alice-${userId.take(8)}",
            userId = userId,
            claimsSnapshot = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        ))

        val loaded = identityRepo.findByProviderAndSubject("local", "alice-${userId.take(8)}")
        assertThat(loaded).isNotNull
        assertThat(loaded!!.userId).isEqualTo(userId)
    }

    @Test
    fun `GroupRecord can be saved and retrieved`() {
        val groupId = UUID.randomUUID().toString()
        groupRepo.save(GroupRecord(groupId = groupId, groupName = "ROLE_ADMIN-${groupId.take(8)}", description = "Admins"))
        val loaded = groupRepo.findById(groupId)
        assertThat(loaded).isPresent
        assertThat(loaded.get().description).isEqualTo("Admins")
    }

    @Test
    fun `GroupMembershipRecord can be saved and retrieved`() {
        val userId = UUID.randomUUID().toString()
        val groupId = UUID.randomUUID().toString()
        userRepo.save(UserRecord(userId = userId, status = UserStatus.ACTIVE.name, displayName = null,
            primaryEmail = null, createdAt = Instant.now(), updatedAt = Instant.now()))
        groupRepo.save(GroupRecord(groupId = groupId, groupName = "ROLE_USER-${groupId.take(8)}", description = null))
        membershipRepo.save(GroupMembershipRecord(id = GroupMembershipKey(userId = userId, groupId = groupId)))

        val memberships = membershipRepo.findGroupsByUserId(userId)
        assertThat(memberships).hasSize(1)
        assertThat(memberships[0].groupId).isEqualTo(groupId)
    }

    @Test
    fun `UserProfileRecord can be saved and retrieved`() {
        val userId = UUID.randomUUID().toString()
        userRepo.save(UserRecord(userId = userId, status = UserStatus.ACTIVE.name, displayName = null,
            primaryEmail = null, createdAt = Instant.now(), updatedAt = Instant.now()))
        profileRepo.save(UserProfileRecord(
            userId = userId,
            displayName = "Alice",
            email = "alice@example.com",
            theme = "dark",
            locale = "en-US",
            updatedAt = Instant.now(),
        ))

        val loaded = profileRepo.findByUserId(userId)
        assertThat(loaded).isNotNull
        assertThat(loaded!!.displayName).isEqualTo("Alice")
        assertThat(loaded.theme).isEqualTo("dark")
        assertThat(loaded.locale).isEqualTo("en-US")
    }

    // --- resolveOrProvision() integration tests ----------------------------

    @Test
    fun `resolveOrProvision creates user, identity, and profile on first call`() {
        val result = service.resolveOrProvision("local", "alice-first")

        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(result.userId).isNotBlank()

        val savedUser = userRepo.findById(result.userId)
        assertThat(savedUser).isPresent

        val savedIdentity = identityRepo.findByProviderAndSubject("local", "alice-first")
        assertThat(savedIdentity).isNotNull
        assertThat(savedIdentity!!.userId).isEqualTo(result.userId)

        val savedProfile = profileRepo.findByUserId(result.userId)
        assertThat(savedProfile).isNotNull
    }

    @Test
    fun `resolveOrProvision returns same userId on second call (idempotent)`() {
        val first = service.resolveOrProvision("local", "alice-idem")
        val second = service.resolveOrProvision("local", "alice-idem")

        assertThat(first.userId).isEqualTo(second.userId)
    }

    @Test
    fun `resolveOrProvision provisions OAuth user with display name and email`() {
        val result = service.resolveOrProvision("entra", "sub-123", "Alice", "a@b.com")

        assertThat(result.displayName).isEqualTo("Alice")
        assertThat(result.primaryEmail).isEqualTo("a@b.com")
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)

        val identity = identityRepo.findByProviderAndSubject("entra", "sub-123")
        assertThat(identity).isNotNull
        assertThat(identity!!.provider).isEqualTo("entra")
    }

    @Test
    fun `resolveOrProvision same OAuth subject provisioned twice returns same userId`() {
        val first = service.resolveOrProvision("entra", "sub-456", "Bob", "bob@example.com")
        val second = service.resolveOrProvision("entra", "sub-456", "Bob Updated", "bob2@example.com")

        assertThat(first.userId).isEqualTo(second.userId)
    }

    @Test
    fun `loadAuthorities returns group names from group_memberships`() {
        val userId = UUID.randomUUID().toString()
        userRepo.save(UserRecord(userId = userId, status = UserStatus.ACTIVE.name, displayName = null,
            primaryEmail = null, createdAt = Instant.now(), updatedAt = Instant.now()))

        val groupId1 = UUID.randomUUID().toString()
        val groupId2 = UUID.randomUUID().toString()
        groupRepo.save(GroupRecord(groupId = groupId1, groupName = "ROLE_ADMIN", description = null))
        groupRepo.save(GroupRecord(groupId = groupId2, groupName = "ROLE_USER", description = null))
        membershipRepo.save(GroupMembershipRecord(id = GroupMembershipKey(userId = userId, groupId = groupId1)))
        membershipRepo.save(GroupMembershipRecord(id = GroupMembershipKey(userId = userId, groupId = groupId2)))

        val authorities = service.loadAuthorities(userId)

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER")
    }
}
