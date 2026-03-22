package io.qpointz.mill.metadata.domain

import java.time.Instant

/**
 * A named, isolated set of metadata facet data — analogous to a branch in version control.
 *
 * Scopes are independent; there is no platform-defined precedence between them. The global
 * scope is the only guaranteed-present scope ([MetadataUrns.SCOPE_GLOBAL]). Beyond that,
 * scope precedence is determined by the caller via [io.qpointz.mill.metadata.service.MetadataContext].
 *
 * The [scopeId] is the canonical Mill URN key. Examples:
 * - `urn:mill/metadata/scope:global`
 * - `urn:mill/metadata/scope:user:alice`
 * - `urn:mill/metadata/scope:team:eng`
 *
 * No `scopeType`, `visibility`, or `referenceId` fields exist on this domain type — those are
 * persistence concerns encoded in the JPA entity. Scope identity is entirely carried by [scopeId].
 *
 * @property scopeId     full Mill scope URN; unique identifier for this scope
 * @property displayName optional human-readable label for the scope
 * @property ownerId     identifier of the user who owns this scope; `null` for the global scope
 * @property createdAt   timestamp when this scope was first created
 */
data class MetadataScope(
    val scopeId: String,
    val displayName: String?,
    val ownerId: String?,
    val createdAt: Instant
)
