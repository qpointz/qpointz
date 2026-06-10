package io.qpointz.mill.ai.autoconfigure.providers

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties
import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.config.PropertiesBackedModelResolver
import io.qpointz.mill.ai.providers.AiProviderRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Registers the default [AiProviderRegistry] bean from `mill.ai.providers` (root bindings live on
 * [AiConfigurationProperties], enabled by [AiConfigurationPropertiesAutoConfiguration]).
 */
@ConditionalOnAiEnabled
@AutoConfiguration
@EnableConfigurationProperties(AiV3ChatProperties::class)
class AiProvidersAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AiProviderRegistry::class)
    fun millAiProviderRegistry(
        properties: AiConfigurationProperties,
    ): AiProviderRegistry = PropertiesBackedAiProviderRegistry(properties)

    @Bean
    @ConditionalOnMissingBean(PropertiesBackedModelResolver::class)
    fun propertiesBackedModelResolver(
        root: AiConfigurationProperties,
        chat: AiV3ChatProperties,
        providers: AiProviderRegistry,
    ): PropertiesBackedModelResolver = PropertiesBackedModelResolver(root, chat, providers)
}
