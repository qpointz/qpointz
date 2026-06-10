package io.qpointz.mill.ai.valuemap

/**
 * One row in `ai_embedding_model` (embedding-spec catalog).
 */
data class AiEmbeddingModelRecord(
    val id: Long,
    val configFingerprint: String,
    val provider: String,
    val modelId: String,
    val dimension: Int,
    val paramsJson: String?,
    val label: String?,
)
