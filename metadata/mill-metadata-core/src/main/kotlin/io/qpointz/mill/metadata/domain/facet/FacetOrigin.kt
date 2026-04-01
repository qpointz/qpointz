package io.qpointz.mill.metadata.domain.facet

/**
 * Provenance of a resolved facet row on the read path (`FacetInstance`).
 *
 * @see io.qpointz.mill.metadata.source.MetadataSource
 */
enum class FacetOrigin {
    /** Persisted assignment; mutable via metadata APIs when policy allows. */
    CAPTURED,

    /** Derived at read time; not a stored assignment. */
    INFERRED
}
