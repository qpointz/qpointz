package io.qpointz.mill.persistence.security.jpa.service

import io.qpointz.mill.persistence.security.jpa.entities.UserIdentityRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserProfileRecord
import io.qpointz.mill.persistence.security.jpa.entities.UserRecord
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserProfileRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.security.domain.ResolvedUser
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import io.qpointz.mill.security.domain.UserStatus
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * JPA implementation of [UserIdentityResolutionService].
 *
 * Resolves and provisions user identities across all authentication methods.
 * Internally operates on JPA entity types ([UserRecord], [UserIdentityRecord],
 * [UserProfileRecord]) but maps to [ResolvedUser] domain objects before returning —
 * callers never see JPA entity types.
 *
 * The `@Transactional` annotation is applied to mutating methods only; it is
 * intentionally absent from the interface definition.
 *
 * @param userRepo repository for canonical user records
 * @param identityRepo repository for provider/subject → userId mappings
 * @param profileRepo repository for user profile records
 * @param membershipRepo repository for group membership resolution
 */
open class JpaUserIdentityResolutionService(
    private val userRepo: UserRepository,
    private val identityRepo: UserIdentityRepository,
    private val profileRepo: UserProfileRepository,
    private val membershipRepo: GroupMembershipRepository,
) : UserIdentityResolutionService {

    /**
     * Looks up an existing user by provider/subject pair.
     *
     * @param provider authentication provider identifier (e.g. `"local"`, `"entra"`)
     * @param subject provider-specific user identifier
     * @return [ResolvedUser] domain object, or `null` if no matching identity exists
     */
    override fun resolve(provider: String, subject: String): ResolvedUser? {
        val identity = identityRepo.findByProviderAndSubject(provider, subject) ?: return null
        val user = userRepo.findById(identity.userId).orElse(null) ?: return null
        return user.toResolvedUser()
    }

    /**
     * Returns an existing user or provisions a new one on first login.
     *
     * On first call for a `(provider, subject)` pair, creates a new [UserRecord],
     * [UserIdentityRecord], and empty [UserProfileRecord]. Subsequent calls with
     * the same pair are idempotent — they return the same [ResolvedUser.userId].
     *
     * @param provider authentication provider identifier
     * @param subject provider-specific user identifier
     * @param displayName optional display name hint — used only when creating a new record
     * @param email optional email hint — used only when creating a new record
     * @return [ResolvedUser] domain object for the resolved or newly provisioned user
     */
    @Transactional
    override fun resolveOrProvision(
        provider: String,
        subject: String,
        displayName: String?,
        email: String?,
    ): ResolvedUser {
        val existing = identityRepo.findByProviderAndSubject(provider, subject)
        if (existing != null) {
            val user = userRepo.findById(existing.userId).orElseThrow {
                IllegalStateException("UserRecord missing for userId=${existing.userId}")
            }
            return user.toResolvedUser()
        }

        val now = Instant.now()
        val userId = UUID.randomUUID().toString()

        val user = userRepo.save(
            UserRecord(
                userId = userId,
                status = UserStatus.ACTIVE.name,
                displayName = displayName,
                primaryEmail = email,
                createdAt = now,
                updatedAt = now,
            )
        )

        identityRepo.save(
            UserIdentityRecord(
                identityId = UUID.randomUUID().toString(),
                provider = provider,
                subject = subject,
                userId = userId,
                claimsSnapshot = null,
                createdAt = now,
                updatedAt = now,
            )
        )

        profileRepo.save(
            UserProfileRecord(
                userId = userId,
                displayName = displayName,
                email = email,
                theme = null,
                locale = null,
                updatedAt = now,
            )
        )

        return user.toResolvedUser()
    }

    /**
     * Loads the group names for a user, used to populate [GrantedAuthority] in Spring Security.
     *
     * @param userId canonical `users.id`
     * @return list of group name strings the user belongs to
     */
    override fun loadAuthorities(userId: String): List<String> =
        membershipRepo.findGroupsByUserId(userId).map { it.groupName }

    private fun UserRecord.toResolvedUser() = ResolvedUser(
        userId = userId,
        displayName = displayName,
        primaryEmail = primaryEmail,
        status = UserStatus.valueOf(status),
    )
}
