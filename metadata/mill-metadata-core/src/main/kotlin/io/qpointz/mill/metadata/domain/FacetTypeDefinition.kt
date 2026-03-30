package io.qpointz.mill.metadata.domain

import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import java.time.Instant

/**
 * Declared contract for a facet type (`metadata_facet_type_def`) — SPEC §5.5.
 *
 * @property typeKey facet type URN
 * @property displayName optional label
 * @property description optional text
 * @property category optional grouping label (UI hint)
 * @property mandatory advisory flag
 * @property enabled whether type is active
 * @property targetCardinality SINGLE vs MULTIPLE per (entity, type, scope) triple
 * @property applicableTo optional entity kind hints; never enforced by core services
 * @property contentSchema optional JSON-schema-like map for client-side validation only
 * @property schemaVersion optional manifest version string
 */
data class FacetTypeDefinition(
    val typeKey: String,
    val displayName: String?,
    val description: String?,
    val category: String? = null,
    val mandatory: Boolean,
    val enabled: Boolean,
    val targetCardinality: FacetTargetCardinality,
    val applicableTo: List<String>?,
    val contentSchema: Map<String, Any?>?,
    val schemaVersion: String?,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
