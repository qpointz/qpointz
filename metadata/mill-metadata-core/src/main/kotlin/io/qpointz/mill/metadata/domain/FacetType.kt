package io.qpointz.mill.metadata.domain

import java.time.Instant

/**
 * Runtime facet type catalog row (`metadata_facet_type`) — SPEC §5.5.
 *
 * @property typeKey facet type URN (unique)
 * @property source DEFINED vs OBSERVED
 * @property definition linked definition when source is DEFINED
 * @property createdAt row creation time
 * @property createdBy actor or null
 * @property lastModifiedAt last mutation time
 * @property lastModifiedBy actor or null
 */
data class FacetType(
    val typeKey: String,
    val source: FacetTypeSource,
    val definition: FacetTypeDefinition?,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
