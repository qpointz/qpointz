package io.qpointz.mill.ai.test.embedding

import io.qpointz.mill.ai.autoconfigure.AiConfigurationPropertiesAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.providers.AiProvidersAutoConfiguration
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration

/**
 * Real OpenAI embedding path (skipped when [OPENAI_API_KEY] is unset).
 */
@SpringBootTest(classes = [OpenAiEmbeddingHarnessIT.HarnessTestApplication::class])
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiEmbeddingHarnessIT {

    @Configuration
    @ImportAutoConfiguration(
        AiConfigurationPropertiesAutoConfiguration::class,
        AiProvidersAutoConfiguration::class,
        EmbeddingAutoConfiguration::class,
    )
    class HarnessTestApplication

    @Autowired
    private lateinit var harness: EmbeddingHarness

    @Test
    fun producesVectorMatchingConfiguredDimension() {
        assertThat(harness.dimension).isEqualTo(1536)
        val v = harness.embed("mill-ai openai embed harness")
        assertThat(v).hasSize(1536)
    }
}
