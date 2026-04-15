package io.qpointz.mill.ai.autoconfigure.embedding

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.config.ValueMappingConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.providers.AiProvidersAutoConfiguration
import io.qpointz.mill.ai.embedding.LangChain4jEmbeddingHarness
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.embedding.stub.DeterministicStubEmbeddingModel
import io.qpointz.mill.ai.providers.AiProviderRegistry
import io.qpointz.mill.ai.runtime.langchain4j.resolvedOpenAiBaseUrl
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Wires [EmbeddingHarness] from `mill.ai.embedding-model` and `mill.ai.value-mapping.embedding-model`.
 */
@ConditionalOnAiEnabled
@AutoConfiguration(after = [AiProvidersAutoConfiguration::class])
@EnableConfigurationProperties(ValueMappingConfigurationProperties::class)
class EmbeddingAutoConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * @param root merged `mill.ai` properties (providers + embedding profiles)
     */
    @Bean
    @ConditionalOnMissingBean(EmbeddingHarness::class)
    fun embeddingHarness(
        root: AiConfigurationProperties,
        vm: ValueMappingConfigurationProperties,
        providers: AiProviderRegistry,
    ): EmbeddingHarness {
        val key = vm.embeddingModel
        val profile = root.embeddingModel[key]
            ?: error("mill.ai.embedding-model['$key'] is not defined")
        log.info(
            "Embedding harness profile='{}' provider='{}' dimension={}",
            key,
            profile.provider,
            profile.dimension,
        )
        val model = buildEmbeddingModel(profile, providers)
        val persistence = LangChain4jEmbeddingHarness.describeForEmbeddingProfile(
            profileKey = key,
            provider = profile.provider,
            modelName = profile.modelName,
            dimension = profile.dimension,
            paramsJson = null,
            label = null,
        )
        return LoggingEmbeddingHarness(LangChain4jEmbeddingHarness(model, persistence))
    }

    private fun buildEmbeddingModel(
        profile: AiConfigurationProperties.EmbeddingModelProfile,
        providers: AiProviderRegistry,
    ): EmbeddingModel {
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
