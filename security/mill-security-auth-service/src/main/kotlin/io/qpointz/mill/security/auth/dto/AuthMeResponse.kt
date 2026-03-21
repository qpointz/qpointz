package io.qpointz.mill.security.auth.dto

/**
 * Response body for `GET /auth/me` and `POST /auth/public/login`.
 *
 * Represents the currently authenticated user in the session. When security is
 * disabled (`securityEnabled = false`), the response represents an anonymous user
 * with `userId = "anonymous"` and empty `groups`.
 *
 * @property userId canonical `users.id` — never an OAuth subject or username; `"anonymous"` when security is off
 * @property email optional primary email address of the resolved user
 * @property displayName optional human-readable display name
 * @property groups list of group names the user belongs to (used as Spring Security authorities)
 * @property securityEnabled whether security is active in this deployment; `false` in security-off mode
 * @property profile optional user-editable profile attributes; `null` for anonymous or security-off responses
 */
data class AuthMeResponse(
    val userId: String,
    val email: String?,
    val displayName: String?,
    val groups: List<String>,
    val securityEnabled: Boolean,
    val profile: UserProfileResponse? = null,
)
