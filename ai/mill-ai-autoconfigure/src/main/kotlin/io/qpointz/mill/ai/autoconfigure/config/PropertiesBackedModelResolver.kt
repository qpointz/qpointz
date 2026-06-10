package io.qpointz.mill.ai.autoconfigure.config

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties
import io.qpointz.mill.ai.embedding.stub.DeterministicStubEmbeddingModel
import io.qpointz.mill.ai.providers.AiProviderRegistry
import io.qpointz.mill.ai.runtime.langchain4j.resolvedOpenAiBaseUrl

/**
 * Builds LangChain4j chat and embedding models from `mill.ai.models.*` and `mill.ai.providers.*`.
 */
class PropertiesBackedModelResolver(
    private val root: AiConfigurationProperties,
    private val chat: AiV3ChatProperties,
    private val providers: AiProviderRegistry,
) {

    fun streamingChatModel(): StreamingChatModel {
        val key = chat.model
        val profile = root.models.chat[key]
            ?: error("mill.ai.models.chat['$key'] is not defined (mill.ai.chat.model='$key')")
        require(profile.provider.equals("openai", ignoreCase = true)) {
            "mill.ai.models.chat['$key'].provider='${profile.provider}' is not supported; only openai in v1"
        }
        val cfg = providers.resolve(profile.provider)
            ?: error("mill.ai.providers['${profile.provider}'] is not defined for chat model '$key'")
        val apiKey = cfg.apiKey
        if (apiKey.isNullOrBlank()) {
            error("mill.ai.providers['${profile.provider}'].api-key is required for chat model '$key'")
        }
        return OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .modelName(profile.modelName)
            .baseUrl(resolvedOpenAiBaseUrl(cfg.baseUrl))
            .build()
    }

    fun embeddingModel(profileKey: String): EmbeddingModel {
        val profile = root.models.embedding[profileKey]
            ?: error("mill.ai.models.embedding['$profileKey'] is not defined")
        return buildEmbeddingModel(profile)
    }

    fun embeddingModelProfile(profileKey: String): AiConfigurationProperties.EmbeddingModelProfile =
        root.models.embedding[profileKey]
            ?: error("mill.ai.models.embedding['$profileKey'] is not defined")

    private fun buildEmbeddingModel(profile: AiConfigurationProperties.EmbeddingModelProfile): EmbeddingModel {
        if (profile.provider.equals("stub", ignoreCase = true)) {
            return DeterministicStubEmbeddingModel(profile.dimension)
        }
        val cfg = providers.resolve(profile.provider)
            ?: error("mill.ai.providers['${profile.provider}'] is not defined for embedding profile")
        val apiKey = cfg.apiKey
        if (apiKey.isNullOrBlank()) {
            error("mill.ai.providers['${profile.provider}'].api-key is required for embedding profile")
        }
        return OpenAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .baseUrl(resolvedOpenAiBaseUrl(cfg.baseUrl))
            .modelName(profile.modelName)
            .dimensions(profile.dimension)
            .build()
    }
}
