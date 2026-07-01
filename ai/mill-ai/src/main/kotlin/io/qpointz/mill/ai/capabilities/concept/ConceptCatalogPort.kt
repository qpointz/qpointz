package io.qpointz.mill.ai.capabilities.concept

import io.qpointz.mill.ai.core.capability.CapabilityDependency

/**
 * Read-only business concept catalog for agent tools.
 */
interface ConceptCatalogPort {

    /**
     * @param scope optional comma-separated metadata scope URNs; when null the adapter uses its default read scope
     * @return distinct tags with assignment counts
     */
    fun listConceptTags(scope: String? = null): List<ConceptTagCount>

    /**
     * @param tag optional exact tag filter (case-insensitive)
     * @param scope optional metadata read scopes
     */
    fun listConcepts(tag: String? = null, scope: String? = null): List<ConceptSummary>

    /**
     * @param conceptRef full concept URN
     * @param scope optional metadata read scopes
     */
    fun getConcept(conceptRef: String, scope: String? = null): ConceptDetail?

    /**
     * Lexical search over concept name, description, and tags.
     *
     * @param query search text
     * @param tag optional exact tag filter
     * @param scope optional metadata read scopes
     */
    fun searchConcepts(query: String, tag: String? = null, scope: String? = null): List<ConceptSummary>

    /**
     * @param scope optional metadata read scopes
     * @return all concepts assigned to the model root in the active scope
     */
    fun getModelConcepts(scope: String? = null): List<ConceptDetail>
}

/** Dependency carrying [ConceptCatalogPort] into the concept capability. */
data class ConceptCapabilityDependency(val catalog: ConceptCatalogPort) : CapabilityDependency

/** Distinct tag with the number of concept assignments that include it. */
data class ConceptTagCount(
    val tag: String,
    val count: Int,
)

/** Compact concept row for browse/search tools. */
data class ConceptSummary(
    val conceptRef: String,
    val slug: String,
    val name: String,
    val description: String?,
    val tags: List<String>,
)

/** Full concept definition resolved from a model-level facet assignment. */
data class ConceptDetail(
    val conceptRef: String,
    val slug: String,
    val name: String,
    val description: String?,
    val sql: String?,
    val tags: List<String>,
    val source: String?,
    val sourceSession: String?,
    val facetUid: String?,
) {
    /** @return compact summary row for browse/search tools */
    fun toSummary(): ConceptSummary =
        ConceptSummary(
            conceptRef = conceptRef,
            slug = slug,
            name = name,
            description = description,
            tags = tags,
        )
}
