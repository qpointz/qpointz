package io.qpointz.mill.ai.autoconfigure.vectorstore

import io.qpointz.mill.ai.autoconfigure.AiConfigurationPropertiesAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.config.VectorStoreConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.providers.AiProvidersAutoConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

class VectorStoreAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(VectorStoreAutoConfiguration::class.java))

    private val embeddingAndVectorRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                AiConfigurationPropertiesAutoConfiguration::class.java,
                AiProvidersAutoConfiguration::class.java,
                EmbeddingAutoConfiguration::class.java,
                VectorStoreAutoConfiguration::class.java,
            ),
        )

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

    @Test
    fun shouldBindPgVectorBackendAndNestedProperties() {
        propertiesOnlyRunner
            .withPropertyValues(
                "mill.ai.vector-store.backend=pgvector",
                "mill.ai.vector-store.pgvector.table=custom_lc_store",
                "mill.ai.vector-store.pgvector.create-table=false",
                "mill.ai.vector-store.pgvector.use-index=true",
                "mill.ai.vector-store.pgvector.index-list-size=64",
            )
            .run { ctx ->
                val p = ctx.getBean(VectorStoreConfigurationProperties::class.java)
                assertThat(p.backend).isEqualTo(VectorStoreConfigurationProperties.Backend.PGVECTOR)
                assertThat(p.pgvector.table).isEqualTo("custom_lc_store")
                assertThat(p.pgvector.isCreateTable).isFalse()
                assertThat(p.pgvector.isUseIndex).isTrue()
                assertThat(p.pgvector.indexListSize).isEqualTo(64)
            }
    }

    @Test
    fun shouldFailWhenPgVectorBackendWithoutDataSource() {
        embeddingAndVectorRunner
            .withPropertyValues(
                "mill.ai.providers.openai.api-key=sk-test",
                "mill.ai.embedding-model.default.provider=stub",
                "mill.ai.embedding-model.default.dimension=8",
                "mill.ai.value-mapping.embedding-model=default",
                "mill.ai.vector-store.backend=pgvector",
            )
            .run { ctx ->
                assertThat(ctx).hasFailed()
                assertThat(ctx.getStartupFailure()!!.cause)
                    .hasMessageContaining("DataSource")
            }
    }

    @Test
    fun shouldFailWhenPgVectorBackendWithH2DataSource() {
        embeddingAndVectorRunner
            .withUserConfiguration(H2DataSourceConfiguration::class.java)
            .withPropertyValues(
                "mill.ai.providers.openai.api-key=sk-test",
                "mill.ai.embedding-model.default.provider=stub",
                "mill.ai.embedding-model.default.dimension=8",
                "mill.ai.value-mapping.embedding-model=default",
                "mill.ai.vector-store.backend=pgvector",
            )
            .run { ctx ->
                assertThat(ctx).hasFailed()
                assertThat(ctx.getStartupFailure()!!.cause)
                    .hasMessageContaining("PostgreSQL")
            }
    }

    @Configuration
    @EnableConfigurationProperties(VectorStoreConfigurationProperties::class)
    open class VectorStorePropertiesTestConfiguration

    @Configuration
    open class H2DataSourceConfiguration {
        @Bean
        fun pgVectorTestDataSource(): DataSource {
            val ds = JdbcDataSource()
            ds.setURL("jdbc:h2:mem:pgvector_store_test;DB_CLOSE_DELAY=-1")
            ds.user = "sa"
            return ds
        }
    }
}
