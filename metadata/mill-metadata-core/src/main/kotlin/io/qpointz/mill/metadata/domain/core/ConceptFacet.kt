package io.qpointz.mill.metadata.domain.core

import io.qpointz.mill.metadata.domain.ConceptSource
import io.qpointz.mill.metadata.domain.MetadataFacet

/** Business concept definitions and their mapped physical targets. */
data class ConceptFacet(
    val concepts: List<Concept> = emptyList(),
    override val facetType: String = "concept",
) : MetadataFacet {

    /** Immutable concept entry persisted under the concept facet. */
    data class Concept(
        val name: String? = null,
        val description: String? = null,
        val sql: String? = null,
        val targets: List<ConceptTarget> = emptyList(),
        val tags: List<String> = emptyList(),
        val category: String? = null,
        val source: ConceptSource? = null,
        val sourceSession: String? = null,
    )
}
