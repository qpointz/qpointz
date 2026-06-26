package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.capabilities.metadata.EmptyMetadataReadPort
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

private val log = LoggerFactory.getLogger(AiV3MetadataReadPortFallbackAutoConfiguration::class.java)

/**
 * Last-resort [MetadataReadPort] when neither [AiV3DataAutoConfiguration] nor the host registered one.
 *
 * Runs after [AiV3DataAutoConfiguration] so the production port from the metadata stack wins when present.
 */
@ConditionalOnAiEnabled
@AutoConfiguration(after = [AiV3DataAutoConfiguration::class])
class AiV3MetadataReadPortFallbackAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MetadataReadPort::class)
    fun emptyMetadataReadPort(): MetadataReadPort {
        log.warn(
            "AI v3: no MetadataReadPort bean; using empty catalog. " +
                "Enable mill-metadata autoconfigure and mill.ai.data wiring for facet tools.",
        )
        return EmptyMetadataReadPort()
    }
}
