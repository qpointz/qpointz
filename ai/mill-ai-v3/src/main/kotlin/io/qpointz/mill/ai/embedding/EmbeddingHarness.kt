package io.qpointz.mill.ai.embedding

/**
 * Turns canonical text into an embedding vector using the configured model profile.
 */
interface EmbeddingHarness {

    /** Vector dimension for the active embedding profile. */
    val dimension: Int

    /**
     * Persistence metadata for registering the same logical model in `ai_embedding_model`, including the
     * [EmbeddingModelPersistenceDescriptor.configFingerprint] formula used by this implementation.
     */
    val persistence: EmbeddingModelPersistenceDescriptor

    /**
     * Produces an embedding vector for [text].
     */
    fun embed(text: String): FloatArray
}
