package io.qpointz.mill.security.auth.dto

/**
 * Request body for `POST /auth/public/login`.
 *
 * @property username the user's login name (mapped from the `email` field in the JSON body for UI compatibility)
 * @property password the raw password supplied by the user
 */
data class LoginRequest(
    val username: String,
    val password: String,
)
