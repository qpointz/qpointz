package io.qpointz.mill.security.domain

/**
 * Canonical resolved user returned by [UserIdentityResolutionService].
 *
 * This is a pure domain type — it carries no persistence-framework annotations.
 * All authentication methods (basic, OAuth, PAT) resolve to this same type
 * through `user_identities`, guaranteeing a stable [userId] regardless of
 * how the user authenticated.
 *
 * @property userId canonical `users.id` — stable UUID, never changes
 * @property displayName optional human-readable display name
 * @property primaryEmail optional primary email address
 * @property status current lifecycle status of the account
 */
data class ResolvedUser(
    val userId: String,
    val displayName: String?,
    val primaryEmail: String?,
    val status: UserStatus,
)
