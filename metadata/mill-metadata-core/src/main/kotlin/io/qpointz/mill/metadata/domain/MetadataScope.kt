package io.qpointz.mill.metadata.domain

import java.time.Instant

/**
 * Facet scope registry row — SPEC §5.6 / §6.4.
 *
 * @property res full scope URN
 * @property scopeType e.g. GLOBAL, USER, TEAM
 * @property referenceId scope local reference or null
 * @property displayName optional label
 * @property ownerId optional owner
 * @property visibility e.g. PUBLIC, PRIVATE
 * @property uuid stable external id from persistence; null before save in some flows
 */
data class MetadataScope(
    val res: String,
    val scopeType: String,
    val referenceId: String?,
    val displayName: String?,
    val ownerId: String?,
    val visibility: String,
    val uuid: String?,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
