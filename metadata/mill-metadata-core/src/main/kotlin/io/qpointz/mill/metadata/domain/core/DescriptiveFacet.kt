package io.qpointz.mill.metadata.domain.core

import com.fasterxml.jackson.annotation.JsonAlias
import io.qpointz.mill.metadata.domain.DataClassification
import io.qpointz.mill.metadata.domain.MetadataFacet

/** Human-readable labels, business context, and tagging for an entity. */
data class DescriptiveFacet(
    @param:JsonAlias("title")
    val displayName: String? = null,
    val description: String? = null,
    val businessMeaning: String? = null,
    val synonyms: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val businessDomain: String? = null,
    val businessOwner: String? = null,
    val classification: DataClassification? = null,
    val unit: String? = null,
    override val facetType: String = "descriptive",
) : MetadataFacet
