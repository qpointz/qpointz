package io.qpointz.mill.metadata.api.dto

import java.time.Instant

/**
 * REST DTO representing a metadata scope.
 *
 * Used for `GET /api/v1/metadata/scopes` and `POST /api/v1/metadata/scopes` endpoints.
 * The [scopeId] is always the full Mill scope URN (e.g. `urn:mill/metadata/scope:global`).
 *
 * @property scopeId     full Mill scope URN key
 * @property displayName optional human-readable label for the scope
 * @property ownerId     optional owner identifier; null for shared or system scopes
 * @property createdAt   timestamp when this scope was created
 */
data class MetadataScopeDto(
    val scopeId: String,
    val displayName: String? = null,
    val ownerId: String? = null,
    val createdAt: Instant? = null
)
