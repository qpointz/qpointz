package io.qpointz.mill.security.domain

/**
 * User profile attributes returned by profile-related operations.
 *
 * This is a pure domain type — it carries no persistence-framework annotations.
 * Created lazily on first access; all fields except [userId] are nullable
 * to accommodate profiles that have not been filled in yet.
 *
 * @property userId canonical `users.id` — matches [ResolvedUser.userId]
 * @property displayName optional human-readable display name
 * @property email optional email address stored in the profile
 * @property locale optional locale string (e.g. `"en-US"`)
 */
data class UserProfile(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val locale: String?,
)
