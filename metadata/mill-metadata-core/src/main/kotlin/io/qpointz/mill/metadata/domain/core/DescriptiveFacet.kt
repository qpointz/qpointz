package io.qpointz.mill.metadata.domain.core

import io.qpointz.mill.metadata.domain.AbstractFacet
import io.qpointz.mill.metadata.domain.DataClassification
import io.qpointz.mill.metadata.domain.MetadataFacet

/** Human-readable labels, business context, and tagging for an entity. */
open class DescriptiveFacet(
    var displayName: String? = null,
    var description: String? = null,
    var businessMeaning: String? = null,
    var synonyms: MutableList<String> = mutableListOf(),
    var aliases: MutableList<String> = mutableListOf(),
    var tags: MutableList<String> = mutableListOf(),
    var businessDomain: String? = null,
    var businessOwner: String? = null,
    var classification: DataClassification? = null,
    var unit: String? = null
) : AbstractFacet() {

    override val facetType: String get() = "descriptive"

    override fun merge(other: MetadataFacet): MetadataFacet {
        if (other !is DescriptiveFacet) return this
        other.displayName?.let { displayName = it }
        other.description?.let { description = it }
        other.businessMeaning?.let { businessMeaning = it }
        other.businessDomain?.let { businessDomain = it }
        other.businessOwner?.let { businessOwner = it }
        other.classification?.let { classification = it }
        other.unit?.let { unit = it }
        mergeList(synonyms, other.synonyms)
        mergeList(aliases, other.aliases)
        mergeList(tags, other.tags)
        return this
    }

    private fun mergeList(target: MutableList<String>, source: List<String>) {
        source.forEach { if (it !in target) target.add(it) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DescriptiveFacet) return false
        return displayName == other.displayName && description == other.description
    }

    override fun hashCode(): Int = (displayName?.hashCode() ?: 0) * 31 + (description?.hashCode() ?: 0)
}
