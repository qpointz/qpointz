package io.qpointz.mill.ai.capabilities.concept

import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns

/**
 * Parses model-level `concept` facet assignments into catalog wire types.
 */
object ConceptFacetParser {

    /**
     * @param facetRows merged facet instances from metadata read port
     * @return parsed concept details keyed by slug
     */
    fun parseConceptFacets(facetRows: List<Map<String, Any?>>): List<ConceptDetail> =
        facetRows.mapNotNull { row -> parseFacetRow(row) }

    private fun parseFacetRow(row: Map<String, Any?>): ConceptDetail? {
        @Suppress("UNCHECKED_CAST")
        val payload = row["payload"] as? Map<String, Any?> ?: return null
        @Suppress("UNCHECKED_CAST")
        val concepts = payload["concepts"] as? List<Map<String, Any?>> ?: return null
        val entry = concepts.firstOrNull() ?: return null
        val name = entry["name"] as? String ?: return null
        val conceptRef = (payload["conceptRef"] as? String)?.trim()?.takeIf { it.isNotEmpty() }
            ?: ConceptRefs.refFromSlug(ConceptRefs.slugFromName(name))
        val slug = runCatching { ConceptRefs.slugFromRef(conceptRef) }.getOrElse {
            ConceptRefs.slugFromName(name)
        }
        return ConceptDetail(
            conceptRef = conceptRef,
            slug = slug,
            name = name,
            description = entry["description"] as? String,
            sql = entry["sql"] as? String,
            tags = (entry["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            source = entry["source"] as? String,
            sourceSession = entry["sourceSession"] as? String,
            facetUid = row["uid"] as? String,
        )
    }

    fun matchesTag(concept: ConceptDetail, tag: String?): Boolean {
        val filter = tag?.trim()?.takeIf { it.isNotEmpty() } ?: return true
        return concept.tags.any { it.equals(filter, ignoreCase = true) }
    }

    fun matchesLexicalQuery(concept: ConceptDetail, query: String): Boolean {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return true
        if (concept.name.lowercase().contains(needle)) return true
        if (concept.description?.lowercase()?.contains(needle) == true) return true
        if (concept.tags.any { it.lowercase().contains(needle) }) return true
        if (concept.slug.contains(needle)) return true
        return false
    }

    fun facetTypeKey(): String = MetadataUrns.FACET_TYPE_CONCEPT

    fun modelEntityId(): String = ModelEntityUrn.MODEL_ENTITY_ID
}
