package io.qpointz.mill.ai.autoconfigure.chat

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * LLM provider and model configuration under `mill.ai.model`.
 *
 * Example YAML:
 * ```yaml
 * mill:
 *   ai:
 *     model:
 *       provider: openai
 *       api-key: ${OPENAI_API_KEY}
 *       model-name: gpt-4o-mini
 *       base-url: https://api.openai.com/v1   # optional; omit or leave blank for the public OpenAI API
 * ```
 */
@ConfigurationProperties("mill.ai.model")
data class AiModelProperties(
    /** LLM provider. Only [Provider.OPENAI] is supported in this release. */
    val provider: Provider = Provider.OPENAI,
    /** API key for the selected provider. */
    val apiKey: String = "",
    /** Model name / deployment id. */
    val modelName: String = "gpt-4o-mini",
    /**
     * Base URL for the OpenAI-compatible HTTP API (typically ends with `/v1`).
     * If null or blank (including after `${OPENAI_BASE_URL:}` with no env), the runtime uses the public OpenAI endpoint.
     */
    val baseUrl: String? = null,
) {
    enum class Provider { OPENAI }
}
