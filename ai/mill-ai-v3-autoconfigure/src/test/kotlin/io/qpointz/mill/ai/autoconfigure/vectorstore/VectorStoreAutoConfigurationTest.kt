package io.qpointz.mill.ai.autoconfigure.vectorstore

import io.qpointz.mill.ai.autoconfigure.config.VectorStoreConfigurationProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

class VectorStoreAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(VectorStoreAutoConfiguration::class.java))

    private val propertiesOnlyRunner = ApplicationContextRunner()
        .withUserConfiguration(VectorStorePropertiesTestConfiguration::class.java)

    @Test
    fun shouldRegisterInMemoryEmbeddingStoreByDefault() {
        runner.run { ctx ->
            assertThat(ctx.containsBean("embeddingStore")).isTrue()
        }
    }

    /**
     * Chroma [dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore] may contact the server during
     * [dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore.Builder.build]; use a properties-only context to
     * verify binding without a live Chroma instance.
     */
    @Test
    fun shouldBindChromaBackendAndNestedProperties() {
        propertiesOnlyRunner
            .withPropertyValues(
                "mill.ai.vector-store.backend=chroma",
                "mill.ai.vector-store.chroma.base-url=http://127.0.0.1:18000",
                "mill.ai.vector-store.chroma.collection-name=mill-test",
                "mill.ai.vector-store.chroma.api-version=v2",
                "mill.ai.vector-store.chroma.timeout=30s",
            )
            .run { ctx ->
                val p = ctx.getBean(VectorStoreConfigurationProperties::class.java)
                assertThat(p.backend).isEqualTo(VectorStoreConfigurationProperties.Backend.CHROMA)
                assertThat(p.chroma.baseUrl).isEqualTo("http://127.0.0.1:18000")
                assertThat(p.chroma.collectionName).isEqualTo("mill-test")
                assertThat(p.chroma.apiVersion).isEqualTo(VectorStoreConfigurationProperties.Chroma.ApiVersion.V2)
                assertThat(p.chroma.timeout.seconds).isEqualTo(30)
            }
    }

    @Test
    fun shouldFailWhenChromaBackendWithoutBaseUrl() {
        runner
            .withPropertyValues("mill.ai.vector-store.backend=chroma")
            .run { ctx ->
                assertThat(ctx).hasFailed()
            }
    }

    @Configuration
    @EnableConfigurationProperties(VectorStoreConfigurationProperties::class)
    open class VectorStorePropertiesTestConfiguration
}
