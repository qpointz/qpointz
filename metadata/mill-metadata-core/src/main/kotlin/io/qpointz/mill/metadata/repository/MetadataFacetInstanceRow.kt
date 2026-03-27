package io.qpointz.mill.metadata.repository

/**
 * One persisted facet payload row ([metadata_facet]) with its stable API identifier.
 *
 * @property facetTypeKey full facet type URN
 * @property scopeKey     full scope URN
 * @property facetUid     unique row id exposed to clients (UUID string)
 * @property sortKey      stable ordering key (DB surrogate); lower values appear first in API lists
 * @property payload      parsed JSON payload for this row
 */
data class MetadataFacetInstanceRow(
    val facetTypeKey: String,
    val scopeKey: String,
    val facetUid: String,
    val sortKey: Long,
    val payload: Any?
)
