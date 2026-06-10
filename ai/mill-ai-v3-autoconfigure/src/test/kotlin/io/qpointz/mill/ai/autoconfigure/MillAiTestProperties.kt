package io.qpointz.mill.ai.autoconfigure

/**
 * Minimal `mill.ai` property sets for autoconfigure unit tests.
 */
object MillAiTestProperties {

    /** Stub embedding pipeline with in-memory vector store. */
    fun stubEmbeddingPipeline(): Array<String> = arrayOf(
        "mill.ai.models.embedding.default.provider=stub",
        "mill.ai.models.embedding.default.dimension=8",
        "mill.ai.chat.value-mapping.embedding=default",
        "mill.ai.data.embedding.default.model=default",
        "mill.ai.data.embedding.default.vector-store.backend=in-memory",
        "mill.ai.data.embedding.default.sources[0].type=metadata-facets",
    )

    /** OpenAI chat model credentials via providers (no duplicate mill.ai.model keys). */
    fun openAiChatModel(): Array<String> = arrayOf(
        "mill.ai.providers.openai.api-key=sk-test",
        "mill.ai.providers.openai.base-url=https://api.openai.com/v1",
        "mill.ai.models.chat.default.provider=openai",
        "mill.ai.models.chat.default.model-name=gpt-4o-mini",
        "mill.ai.chat.model=default",
    )

    fun minimalAiStack(): Array<String> = openAiChatModel() + stubEmbeddingPipeline()
}
