package io.qpointz.mill.security.auth.dto

/**
 * Partial-update body for `PATCH /auth/profile`.
 *
 * Only non-null fields are applied to the stored profile; fields sent as
 * JSON `null` (or omitted entirely) are left unchanged. This follows a
 * standard JSON-merge-patch approach without requiring a separate PATCH
 * media-type.
 *
 * @property displayName new display name to set, or `null` to leave unchanged
 * @property email new email address to set, or `null` to leave unchanged
 * @property locale new locale string to set (e.g. `"en"`, `"fr"`), or `null` to leave unchanged
 */
data class UserProfilePatch(
    val displayName: String?,
    val email: String?,
    val locale: String?,
)
