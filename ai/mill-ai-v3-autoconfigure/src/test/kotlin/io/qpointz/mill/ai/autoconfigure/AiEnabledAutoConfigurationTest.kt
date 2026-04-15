package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.providers.AiProvidersAutoConfiguration
import io.qpointz.mill.ai.providers.AiProviderRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AiEnabledAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                AiConfigurationPropertiesAutoConfiguration::class.java,
                AiProvidersAutoConfiguration::class.java,
                EmbeddingAutoConfiguration::class.java,
            ),
        )

    @Test
    fun shouldSkipAiBeansWhenDisabled() {
        runner
            .withPropertyValues("mill.ai.enabled=false")
            .run { ctx ->
                assertThat(ctx).hasSingleBean(AiConfigurationProperties::class.java)
                assertThat(ctx.getBean(AiConfigurationProperties::class.java).isEnabled).isFalse()
                assertThat(ctx).doesNotHaveBean(AiProviderRegistry::class.java)
            }
    }

    @Test
    fun shouldRegisterAiProviderRegistryWhenEnabledByDefault() {
        runner
            .withPropertyValues(
                "mill.ai.providers.openai.api-key=sk-test",
                "mill.ai.embedding-model.default.provider=stub",
                "mill.ai.embedding-model.default.dimension=8",
                "mill.ai.value-mapping.embedding-model=default",
            )
            .run { ctx ->
                assertThat(ctx.getBean(AiConfigurationProperties::class.java).isEnabled).isTrue()
                assertThat(ctx).hasSingleBean(AiProviderRegistry::class.java)
            }
    }
}
