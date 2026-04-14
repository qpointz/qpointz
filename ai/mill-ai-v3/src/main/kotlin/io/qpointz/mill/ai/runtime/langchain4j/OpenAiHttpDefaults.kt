package io.qpointz.mill.ai.runtime.langchain4j

/**
 * Default OpenAI-compatible HTTP API base URL (`…/v1`).
 *
 * LangChain4j's OpenAI client rejects a null or blank base URL. Hosts that omit
 * `mill.ai.model.base-url` or set `OPENAI_BASE_URL` to empty should still resolve to the
 * public OpenAI endpoint unless a non-blank override is provided.
 */
const val DEFAULT_OPENAI_API_BASE_URL: String = "https://api.openai.com/v1"

/**
 * Returns [baseUrl] with surrounding whitespace removed if the result is non-empty;
 * otherwise [DEFAULT_OPENAI_API_BASE_URL].
 *
 * @param baseUrl optional override (may be null, or blank after env/YAML binding)
 */
fun resolvedOpenAiBaseUrl(baseUrl: String?): String =
    baseUrl?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_OPENAI_API_BASE_URL
