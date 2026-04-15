package io.qpointz.mill.ai.autoconfigure.vectorstore

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class VectorStoreAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(VectorStoreAutoConfiguration::class.java))

    @Test
    fun shouldRegisterInMemoryEmbeddingStoreByDefault() {
        runner.run { ctx ->
            assertThat(ctx.containsBean("embeddingStore")).isTrue()
        }
    }
}
