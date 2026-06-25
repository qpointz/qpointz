package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.config.AiProfileSeedProperties
import io.qpointz.mill.ai.autoconfigure.config.DataEmbeddingConfigurationProperties
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

/**
 * Always loads so `mill.ai` (including [AiConfigurationProperties.enabled]) binds even when
 * the rest of AI autoconfigure is switched off via `mill.ai.enabled=false`.
 */
@AutoConfiguration
@EnableConfigurationProperties(
    AiConfigurationProperties::class,
    DataEmbeddingConfigurationProperties::class,
    AiProfileSeedProperties::class,
)
class AiConfigurationPropertiesAutoConfiguration
