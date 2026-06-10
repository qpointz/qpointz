package io.qpointz.mill.ai.providers

/**
 * Resolved credentials and HTTP settings for a logical provider id (e.g. `openai`).
 *
 * @property apiKey API key when configured; may be blank for stubbed local flows.
 * @property baseUrl Optional OpenAI-compatible base URL; null or blank means use the provider default.
 */
data class AiProviderConfig(
    val apiKey: String?,
    val baseUrl: String?,
)

/**
 * Resolves [AiProviderConfig] by provider id from `mill.ai.providers.*`.
 */
fun interface AiProviderRegistry {

    /**
     * Returns configuration for [providerId], or `null` if the id is unknown.
     */
    fun resolve(providerId: String): AiProviderConfig?
}
