package io.qpointz.mill.ai.data.concept

import io.qpointz.mill.ai.capabilities.concept.ConceptCatalogPort
import io.qpointz.mill.ai.capabilities.concept.ConceptDetail
import io.qpointz.mill.ai.capabilities.concept.ConceptFacetParser
import io.qpointz.mill.ai.capabilities.concept.ConceptRefs
import io.qpointz.mill.ai.capabilities.concept.ConceptSummary
import io.qpointz.mill.ai.capabilities.concept.ConceptTagCount
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort

/**
 * Production [ConceptCatalogPort] backed by model-level `concept` facet assignments.
 *
 * @param metadataReadPort merged facet reads from the metadata stack
 */
class ServiceConceptCatalogAdapter(
    private val metadataReadPort: MetadataReadPort,
) : ConceptCatalogPort {

    override fun listConceptTags(scope: String?): List<ConceptTagCount> {
        val counts = linkedMapOf<String, Int>()
        loadConcepts(scope).forEach { concept ->
            concept.tags.forEach { tag ->
                val key = tag.lowercase()
                counts[key] = (counts[key] ?: 0) + 1
            }
        }
        return counts.entries
            .sortedBy { it.key }
            .map { (tag, count) -> ConceptTagCount(tag = tag, count = count) }
    }

    override fun listConcepts(tag: String?, scope: String?): List<ConceptSummary> =
        loadConcepts(scope)
            .filter { ConceptFacetParser.matchesTag(it, tag) }
            .map { it.toSummary() }
            .sortedBy { it.slug }

    override fun getConcept(conceptRef: String, scope: String?): ConceptDetail? {
        val canonical = ConceptRefs.parse(conceptRef)
        val slug = ConceptRefs.slugFromRef(canonical)
        return loadConcepts(scope).firstOrNull { it.slug == slug }
    }

    override fun searchConcepts(query: String, tag: String?, scope: String?): List<ConceptSummary> =
        loadConcepts(scope)
            .filter { ConceptFacetParser.matchesTag(it, tag) }
            .filter { ConceptFacetParser.matchesLexicalQuery(it, query) }
            .map { it.toSummary() }
            .sortedBy { it.slug }

    override fun getModelConcepts(scope: String?): List<ConceptDetail> =
        loadConcepts(scope).sortedBy { it.slug }

    private fun loadConcepts(scope: String?): List<ConceptDetail> {
        val conceptType = ConceptFacetParser.facetTypeKey()
        val rows = metadataReadPort.listEntityFacets(
            metadataEntityId = ConceptFacetParser.modelEntityId(),
            scope = scope,
            context = null,
            origin = null,
        ).filter { row ->
            val typeUrn = row["facetTypeUrn"] as? String ?: return@filter false
            typeUrn == conceptType || typeUrn.endsWith(":concept")
        }
        return ConceptFacetParser.parseConceptFacets(rows).distinctBy { it.slug }
    }
}

/** Adapts [MetadataReadPort] to [ConceptCatalogPort] for non-Spring call sites. */
fun MetadataReadPort.asConceptCatalogPort(): ConceptCatalogPort = ServiceConceptCatalogAdapter(this)
