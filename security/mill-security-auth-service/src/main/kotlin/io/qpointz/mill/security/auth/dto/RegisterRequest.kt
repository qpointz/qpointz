package io.qpointz.mill.security.auth.dto

/**
 * Request body for `POST /auth/public/register`.
 *
 * Carries the credentials and optional display name required to create a new local
 * (password-based) user account. The [email] field is used as the authentication
 * subject; [displayName] defaults to [email] when absent.
 *
 * @property email the user's email address — used as the local-auth subject and stored
 *   as the primary email on the canonical user record
 * @property password the plaintext password chosen by the user — hashed before storage;
 *   must be non-blank
 * @property displayName optional human-readable name shown in the UI; when `null` or
 *   blank the backend falls back to [email]
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String?,
)
