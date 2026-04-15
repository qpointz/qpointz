package io.qpointz.mill.ai.autoconfigure.providers

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.providers.AiProviderRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Registers the default [AiProviderRegistry] bean from `mill.ai.providers` (root bindings live on
 * [AiConfigurationProperties], enabled by [AiConfigurationPropertiesAutoConfiguration]).
 */
@ConditionalOnAiEnabled
@AutoConfiguration
class AiProvidersAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AiProviderRegistry::class)
    fun millAiProviderRegistry(
        properties: AiConfigurationProperties,
    ): AiProviderRegistry = PropertiesBackedAiProviderRegistry(properties)
}
