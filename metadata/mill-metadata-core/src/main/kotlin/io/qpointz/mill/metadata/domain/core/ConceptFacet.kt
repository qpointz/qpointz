package io.qpointz.mill.metadata.domain.core

import io.qpointz.mill.metadata.domain.AbstractFacet
import io.qpointz.mill.metadata.domain.ConceptSource
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.ValidationException

/** Business concept definitions and their mapped physical targets. */
open class ConceptFacet(
    var concepts: MutableList<Concept> = mutableListOf()
) : AbstractFacet() {

    /** Immutable concept entry persisted under the concept facet. */
    data class Concept(
        val name: String? = null,
        val description: String? = null,
        val sql: String? = null,
        val targets: List<ConceptTarget> = emptyList(),
        val tags: List<String> = emptyList(),
        val category: String? = null,
        val source: ConceptSource? = null,
        val sourceSession: String? = null
    )

    override val facetType: String get() = "concept"

    override fun validate() {
        for (c in concepts) {
            if (c.name.isNullOrEmpty()) throw ValidationException("ConceptFacet: concept name is required")
        }
    }

    override fun merge(other: MetadataFacet): MetadataFacet {
        if (other !is ConceptFacet) return this
        if (other.concepts.isNotEmpty()) {
            val merged = concepts.toMutableList()
            for (otherC in other.concepts) {
                if (merged.none { it.name == otherC.name }) merged.add(otherC)
            }
            concepts = merged
        }
        return this
    }

    override fun equals(other: Any?): Boolean = this === other || (other is ConceptFacet && concepts == other.concepts)
    override fun hashCode(): Int = concepts.hashCode()
}
