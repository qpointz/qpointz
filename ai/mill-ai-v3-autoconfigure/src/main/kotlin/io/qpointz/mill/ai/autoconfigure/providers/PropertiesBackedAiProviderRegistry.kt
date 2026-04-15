package io.qpointz.mill.ai.autoconfigure.providers

import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.providers.AiProviderConfig
import io.qpointz.mill.ai.providers.AiProviderRegistry
import org.slf4j.LoggerFactory

/**
 * [AiProviderRegistry] backed by [AiConfigurationProperties] {@code providers} map.
 */
class PropertiesBackedAiProviderRegistry(
    private val properties: AiConfigurationProperties,
) : AiProviderRegistry {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun resolve(providerId: String): AiProviderConfig? {
        val entry = properties.providers[providerId] ?: return null
        log.debug("Resolved mill.ai.providers.{} (apiKey present: {})", providerId, !entry.apiKey.isNullOrBlank())
        return AiProviderConfig(
            apiKey = entry.apiKey?.takeIf { it.isNotBlank() },
            baseUrl = entry.baseUrl?.takeIf { it.isNotBlank() },
        )
    }
}
