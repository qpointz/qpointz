package io.qpointz.mill.ai.autoconfigure.providers

import dev.langchain4j.model.chat.StreamingChatModel
import io.qpointz.mill.ai.autoconfigure.AiConfigurationPropertiesAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.AiV3AutoConfiguration
import io.qpointz.mill.ai.autoconfigure.MillAiTestProperties
import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AiProvidersConfigurationPropertiesTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                AiConfigurationPropertiesAutoConfiguration::class.java,
                AiProvidersAutoConfiguration::class.java,
            ),
        )

    private val embeddingRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                AiConfigurationPropertiesAutoConfiguration::class.java,
                AiProvidersAutoConfiguration::class.java,
                EmbeddingAutoConfiguration::class.java,
            ),
        )

    private val chatRunner = ApplicationContextRunner()
        .withClassLoader(FilteredClassLoader("io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatMemoryStore"))
        .withConfiguration(
            AutoConfigurations.of(
                AiConfigurationPropertiesAutoConfiguration::class.java,
                AiProvidersAutoConfiguration::class.java,
                AiV3AutoConfiguration::class.java,
            ),
        )

    @Test
    fun shouldBindProviderEntriesFromAiYaml() {
        contextRunner
            .withPropertyValues(
                "mill.ai.providers.openai.type=openai",
                "mill.ai.providers.openai.api-key=sk-test",
                "mill.ai.providers.openai.base-url=https://api.openai.com/v1",
            )
            .run { ctx ->
                val props = ctx.getBean(AiConfigurationProperties::class.java)
                assertThat(props.providers["openai"]?.type).isEqualTo("openai")
                assertThat(props.providers["openai"]?.apiKey).isEqualTo("sk-test")
                assertThat(props.providers["openai"]?.baseUrl).isEqualTo("https://api.openai.com/v1")

                val registry = ctx.getBean(io.qpointz.mill.ai.providers.AiProviderRegistry::class.java)
                val cfg = registry.resolve("openai")!!
                assertThat(cfg.apiKey).isEqualTo("sk-test")
                assertThat(cfg.baseUrl).isEqualTo("https://api.openai.com/v1")
            }
    }

    @Test
    fun shouldReturnNullWhenProviderIdUnknown() {
        contextRunner
            .withPropertyValues("mill.ai.providers.openai.api-key=x")
            .run { ctx ->
                val registry = ctx.getBean(io.qpointz.mill.ai.providers.AiProviderRegistry::class.java)
                assertThat(registry.resolve("unknown")).isNull()
            }
    }

    @Test
    fun shouldWireStubEmbeddingHarnessFromProfiles() {
        embeddingRunner
            .withPropertyValues(*MillAiTestProperties.stubEmbeddingPipeline())
            .run { ctx ->
                val harness = ctx.getBean(EmbeddingHarness::class.java)
                assertThat(harness.dimension).isEqualTo(8)
                assertThat(harness.persistence.configFingerprint).isEqualTo("stub|8|text-embedding-3-small|default")
                val a = harness.embed("hello")
                val b = harness.embed("hello")
                assertThat(a).hasSize(8)
                assertThat(a).containsExactly(*b)
            }
    }

    @Test
    fun shouldResolveChatModelCredentialsOnlyFromProviders() {
        chatRunner
            .withPropertyValues(*MillAiTestProperties.openAiChatModel())
            .run { ctx ->
                assertThat(ctx).hasSingleBean(StreamingChatModel::class.java)
                val root = ctx.getBean(AiConfigurationProperties::class.java)
                assertThat(root.providers["openai"]?.apiKey).isEqualTo("sk-test")
            }
    }
}
