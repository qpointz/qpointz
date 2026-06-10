package io.qpointz.mill.ai.autoconfigure.embedding

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties
import io.qpointz.mill.ai.autoconfigure.config.ActiveDataEmbeddingProfileResolver
import io.qpointz.mill.ai.autoconfigure.config.DataEmbeddingConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.config.PropertiesBackedModelResolver
import io.qpointz.mill.ai.autoconfigure.providers.AiProvidersAutoConfiguration
import io.qpointz.mill.ai.embedding.LangChain4jEmbeddingHarness
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Wires [EmbeddingHarness] from `mill.ai.data.embedding`, `mill.ai.models.embedding`, and `mill.ai.providers`.
 */
@ConditionalOnAiEnabled
@AutoConfiguration(after = [AiProvidersAutoConfiguration::class])
class EmbeddingAutoConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnMissingBean(ActiveDataEmbeddingProfileResolver::class)
    fun activeDataEmbeddingProfileResolver(
        chat: AiV3ChatProperties,
        data: DataEmbeddingConfigurationProperties,
    ): ActiveDataEmbeddingProfileResolver = ActiveDataEmbeddingProfileResolver(chat, data)

    /**
     * @param profileResolver active `mill.ai.data.embedding` profile (via `chat.value-mapping.embedding`)
     */
    @Bean
    @ConditionalOnMissingBean(EmbeddingHarness::class)
    fun embeddingHarness(
        profileResolver: ActiveDataEmbeddingProfileResolver,
        modelResolver: PropertiesBackedModelResolver,
    ): EmbeddingHarness {
        profileResolver.validateProfile()
        val dataProfile = profileResolver.profile()
        val profileId = profileResolver.profileId()
        val modelKey = dataProfile.model
        val modelProfile = modelResolver.embeddingModelProfile(modelKey)
        log.info(
            "Embedding harness dataProfile='{}' model='{}' provider='{}' dimension={}",
            profileId,
            modelKey,
            modelProfile.provider,
            modelProfile.dimension,
        )
        val model = modelResolver.embeddingModel(modelKey)
        val persistence = LangChain4jEmbeddingHarness.describeForEmbeddingProfile(
            profileKey = profileId,
            provider = modelProfile.provider,
            modelName = modelProfile.modelName,
            dimension = modelProfile.dimension,
            paramsJson = null,
            label = null,
        )
        return LoggingEmbeddingHarness(LangChain4jEmbeddingHarness(model, persistence))
    }
}
