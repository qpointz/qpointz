package io.qpointz.mill.ai.capabilities.concept

/**
 * [ConceptCatalogPort] that returns no concepts when the metadata stack is absent.
 */
object EmptyConceptCatalogPort : ConceptCatalogPort {
    override fun listConceptTags(scope: String?): List<ConceptTagCount> = emptyList()

    override fun listConcepts(tag: String?, scope: String?): List<ConceptSummary> = emptyList()

    override fun getConcept(conceptRef: String, scope: String?): ConceptDetail? = null

    override fun searchConcepts(query: String, tag: String?, scope: String?): List<ConceptSummary> = emptyList()

    override fun getModelConcepts(scope: String?): List<ConceptDetail> = emptyList()
}
