package io.qpointz.mill.security.auth.dto

/**
 * Structured error body returned on authentication failures.
 *
 * @property status HTTP status code (e.g. `401`)
 * @property error short error category (e.g. `"Unauthorized"`)
 * @property message human-readable description of the error
 */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
)
