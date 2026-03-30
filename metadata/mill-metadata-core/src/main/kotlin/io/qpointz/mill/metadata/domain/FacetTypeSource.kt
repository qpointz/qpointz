package io.qpointz.mill.metadata.domain

/** Origin of a runtime [FacetType] row (SPEC §5.5). */
enum class FacetTypeSource {
    /** A [FacetTypeDefinition] exists; runtime row is linked to def. */
    DEFINED,

    /** No definition registered; created when assignments use an unknown type key. */
    OBSERVED
}
