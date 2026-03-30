package io.qpointz.mill.metadata.domain

import java.time.Instant

/**
 * Metadata entity identity (SPEC §5.3). Coordinates live in the data layer / facet payloads only.
 *
 * @property id full entity URN (`entity_res`)
 * @property kind opaque label (e.g. table, attribute); null if untyped
 * @property uuid stable external id from persistence; null before first save
 * @property createdAt creation time
 * @property createdBy actor id or null
 * @property lastModifiedAt last mutation time
 * @property lastModifiedBy last actor id or null
 */
data class MetadataEntity(
    val id: String,
    val kind: String?,
    val uuid: String?,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
