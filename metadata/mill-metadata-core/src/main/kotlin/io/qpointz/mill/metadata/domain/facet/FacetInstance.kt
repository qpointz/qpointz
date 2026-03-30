package io.qpointz.mill.metadata.domain.facet

import java.time.Instant

/**
 * Domain row for a single facet assignment (SPEC §5.4).
 *
 * [uid] is the stable external id (UUID) for this assignment; immutable after first persist.
 * [entityId] is the full entity URN stored in `metadata_entity.entity_res`.
 *
 * @property uid stable assignment UUID
 * @property entityId entity instance URN
 * @property facetTypeKey facet type URN (`urn:mill/metadata/facet-type:…`)
 * @property scopeKey scope URN
 * @property mergeAction overlay merge semantics for this row
 * @property payload facet JSON payload (opaque to core merge rules beyond cardinality)
 * @property createdAt row creation time
 * @property createdBy actor id or null
 * @property lastModifiedAt last mutation time
 * @property lastModifiedBy last actor id or null
 */
data class FacetInstance(
    val uid: String,
    val entityId: String,
    val facetTypeKey: String,
    val scopeKey: String,
    val mergeAction: MergeAction,
    val payload: Map<String, Any?>,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
