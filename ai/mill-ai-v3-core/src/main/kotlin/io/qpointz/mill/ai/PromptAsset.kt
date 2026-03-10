package io.qpointz.mill.ai

/**
 * Prompt assets are kept explicit so they can later be described and exposed independently of
 * the code that consumes them.
 */
data class PromptAsset(
    val id: String,
    val description: String,
    val content: String,
)
