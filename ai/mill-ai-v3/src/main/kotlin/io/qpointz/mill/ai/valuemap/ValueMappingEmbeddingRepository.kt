package io.qpointz.mill.ai.valuemap

import java.util.UUID

/**
 * Persistence port for `ai_embedding_model` and `ai_value_mapping` (WI-174).
 */
interface ValueMappingEmbeddingRepository {

    /**
     * Returns the surrogate key for [configFingerprint], or `null` if none.
     */
    fun findEmbeddingModelIdByFingerprint(configFingerprint: String): Long?

    /**
     * Ensures an `ai_embedding_model` row exists for [configFingerprint]: creates it when absent,
     * otherwise returns the existing row's surrogate id (idempotent; safe under concurrent callers).
     */
    fun ensureEmbeddingModel(
        configFingerprint: String,
        provider: String,
        modelId: String,
        dimension: Int,
        paramsJson: String?,
        label: String?,
    ): Long

    /**
     * All value rows for [attributeUrn].
     */
    fun listValueRowsByAttributeUrn(attributeUrn: String): List<AiValueMappingRecord>

    /**
     * Looks up by natural key ([attributeUrn], [content]).
     */
    fun findValueRow(attributeUrn: String, content: String): AiValueMappingRecord?

    /**
     * Inserts or updates a row for ([attributeUrn], [content]); returns [stableId] used.
     */
    fun upsertValueRow(
        stableId: UUID?,
        attributeUrn: String,
        content: String,
        contentHash: String?,
        embedding: FloatArray?,
        embeddingModelId: Long,
        metadataJson: String?,
    ): UUID

    /**
     * Deletes a value row by primary key.
     */
    fun deleteValueRow(stableId: UUID)
}
