package io.qpointz.mill.persistence.security.jpa.auth

import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.security.authentication.basic.providers.User
import io.qpointz.mill.security.authentication.basic.providers.UserRepo

/**
 * JPA-backed implementation of [UserRepo] for basic/local authentication.
 *
 * Resolves users by enumerating all `user_identities` rows with `provider = "local"`,
 * then loading each user's enabled credential and group memberships. Only users with
 * an enabled [io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord]
 * are included — OAuth-only users and users with disabled credentials are silently skipped.
 *
 * The returned [User] instances carry the stored `{prefix}encoded` password hash so that
 * [io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider]
 * can delegate verification to
 * [org.springframework.security.crypto.factory.PasswordEncoderFactories].
 *
 * @param identityRepo repository for provider/subject → userId mappings
 * @param credentialRepo repository for password credentials
 * @param membershipRepo repository for group membership resolution
 * @param userRepo repository for canonical user records
 */
open class JpaUserRepo(
    private val identityRepo: UserIdentityRepository,
    private val credentialRepo: UserCredentialRepository,
    private val membershipRepo: GroupMembershipRepository,
    private val userRepo: UserRepository,
) : UserRepo() {

    /**
     * Returns all local users that may authenticate with a password.
     *
     * Queries `user_identities` for all rows with `provider = "local"`, then for each
     * identity loads the corresponding
     * [io.qpointz.mill.persistence.security.jpa.entities.UserRecord],
     * the enabled [io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord],
     * and the group names from `group_memberships`.
     *
     * A user is excluded (returns `null` and is silently skipped) if any of the following
     * conditions are true:
     * - No enabled credential record exists (`user_credentials.enabled = false` or missing).
     * - The canonical [io.qpointz.mill.persistence.security.jpa.entities.UserRecord] is missing.
     * - [io.qpointz.mill.persistence.security.jpa.entities.UserRecord.validated] is `false`
     *   (email not yet confirmed).
     * - [io.qpointz.mill.persistence.security.jpa.entities.UserRecord.locked] is `true`
     *   (account administratively locked).
     *
     * @return list of [User] objects ready for password verification by
     *   [io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider]
     */
    override fun getUsers(): List<User> =
        identityRepo.findByProvider("local").mapNotNull { identity ->
            val user = userRepo.findById(identity.userId).orElse(null) ?: return@mapNotNull null
            if (!user.validated || user.locked) return@mapNotNull null
            val credential = credentialRepo.findByUserIdAndEnabledTrue(identity.userId)
                ?: return@mapNotNull null
            val groups = membershipRepo.findGroupsByUserId(identity.userId).map { it.groupName }
            User.builder()
                .name(identity.subject)
                .password(credential.passwordHash)
                .groups(groups)
                .build()
        }
}
