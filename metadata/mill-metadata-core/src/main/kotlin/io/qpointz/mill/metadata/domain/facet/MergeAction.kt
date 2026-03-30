package io.qpointz.mill.metadata.domain.facet

/**
 * Persisted merge contribution for a facet assignment row (`metadata_entity_facet.merge_action`).
 *
 * Semantics: see story SPEC §8.3 / §10.2 (overlay resolution and HTTP unassign rules).
 */
enum class MergeAction {
    /** Normal overlay: payload applies at this scope; last-wins across [io.qpointz.mill.metadata.service.MetadataContext]. */
    SET,

    /** While this scope is active, suppresses inherited SET from earlier scopes for this facet type. */
    TOMBSTONE,

    /** No effective contribution (row may remain for audit). */
    CLEAR
}
