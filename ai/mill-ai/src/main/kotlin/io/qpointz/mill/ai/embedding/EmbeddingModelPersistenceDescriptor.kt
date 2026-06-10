package io.qpointz.mill.ai.embedding

/**
 * Values aligned with [io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository.ensureEmbeddingModel]
 * for the active embedding harness. Populated by each [EmbeddingHarness] implementation (for example
 * [LangChain4jEmbeddingHarness] builds a fingerprint from the Mill AI embedding profile).
 */
data class EmbeddingModelPersistenceDescriptor(
    /** Stable key for deduplicating `ai_embedding_model` rows (implementation-defined format). */
    val configFingerprint: String,
    val provider: String,
    /** Remote or logical model id (e.g. OpenAI `model` name). */
    val modelId: String,
    val dimension: Int,
    val paramsJson: String?,
    /** Optional display label stored alongside the model row. */
    val label: String?,
)
