package io.qpointz.mill.ai.valuemap

/**
 * WI-179 reconciliation between value entries, [ValueMappingEmbeddingRepository], and LangChain4j [dev.langchain4j.store.embedding.EmbeddingStore].
 */
fun interface VectorMappingSynchronizer {

    /**
     * Runs Phase A (orphans) and Phase B (embed / upsert / re-ingest) for [attributeUrn].
     *
     * @param embeddingModelId surrogate key of the active `ai_embedding_model` row for this run.
     */
    fun sync(
        attributeUrn: String,
        entries: List<AttributeValueEntry>,
        embeddingModelId: Long,
    )
}
