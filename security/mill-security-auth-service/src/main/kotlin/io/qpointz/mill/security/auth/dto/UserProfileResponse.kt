package io.qpointz.mill.security.auth.dto

/**
 * Response body carrying a user's editable profile attributes.
 *
 * Returned by `GET /auth/me` (nested in [AuthMeResponse]) and by
 * `PATCH /auth/profile` after a successful update.
 *
 * @property userId canonical `users.id` — never mutable by the caller
 * @property displayName optional human-readable display name chosen by the user
 * @property email optional email address stored in the profile
 * @property locale optional locale string (e.g. `"en"`, `"fr"`)
 */
data class UserProfileResponse(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val locale: String?,
)
