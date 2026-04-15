package io.qpointz.mill.ai.valuemap

/**
 * Facade for value-mapping vector hydration (WI-180); delegates sync to [VectorMappingSynchronizer].
 */
interface ValueMappingService {

    /**
     * Last-wins deduplication on [AttributeValueEntry.content] is applied before [VectorMappingSynchronizer.sync].
     *
     * @param embeddingModelId active `ai_embedding_model.id` for this run.
     */
    fun syncAttribute(
        attributeUrn: String,
        entries: List<AttributeValueEntry>,
        embeddingModelId: Long,
    )
}
