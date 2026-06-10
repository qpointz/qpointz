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

    /**
     * Indexes from a [ValueSource]: dedupe (last-wins on content), optional [maxContentLength] truncation,
     * then [VectorMappingSynchronizer.sync]. Invokes [progress] per WI-184 § Integration when non-null.
     */
    fun syncFromSource(
        attributeUrn: String,
        source: ValueSource,
        embeddingModelId: Long,
        progress: ValueMappingSyncProgressCallback?,
    )
}
