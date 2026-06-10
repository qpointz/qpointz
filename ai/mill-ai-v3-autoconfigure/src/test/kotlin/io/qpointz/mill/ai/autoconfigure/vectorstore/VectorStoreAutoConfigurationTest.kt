package io.qpointz.mill.ai.autoconfigure.vectorstore

import io.qpointz.mill.ai.autoconfigure.AiConfigurationPropertiesAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.MillAiTestProperties
import io.qpointz.mill.ai.autoconfigure.config.AiConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.config.DataEmbeddingConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.config.VectorStoreConfigMerger
import io.qpointz.mill.ai.autoconfigure.config.VectorStoreSettings
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
        embeddingAndVectorRunner
            .withPropertyValues(*MillAiTestProperties.minimalAiStack())
            .run { ctx ->
                assertThat(ctx.containsBean("embeddingStore")).isTrue()
            }
    }

    @Test
    fun shouldBindChromaBackendOnDataEmbeddingProfile() {
        propertiesOnlyRunner
            .withPropertyValues(
                "mill.ai.data.embedding.default.vector-store.backend=chroma",
                "mill.ai.data.embedding.default.vector-store.chroma.base-url=http://127.0.0.1:18000",
                "mill.ai.data.embedding.default.vector-store.chroma.collection-name=mill-test",
                "mill.ai.data.embedding.default.vector-store.chroma.api-version=v2",
                "mill.ai.data.embedding.default.vector-store.chroma.timeout=30s",
            )
            .run { ctx ->
                val profile = ctx.getBean(DataEmbeddingConfigurationProperties::class.java).embedding["default"]!!
                assertThat(profile.vectorStore.backend).isEqualTo("chroma")
                assertThat(profile.vectorStore.chroma.baseUrl).isEqualTo("http://127.0.0.1:18000")
                assertThat(profile.vectorStore.chroma.collectionName).isEqualTo("mill-test")
                assertThat(profile.vectorStore.chroma.apiVersion).isEqualTo(VectorStoreSettings.Chroma.ApiVersion.V2)
                assertThat(profile.vectorStore.chroma.timeout.seconds).isEqualTo(30)
            }
    }

    @Test
    fun shouldFailWhenChromaBackendWithoutBaseUrl() {
        embeddingAndVectorRunner
            .withPropertyValues(
                *MillAiTestProperties.stubEmbeddingPipeline(),
                "mill.ai.data.embedding.default.vector-store.backend=chroma",
            )
            .run { ctx ->
                assertThat(ctx).hasFailed()
            }
    }

    @Test
    fun shouldBindPgVectorBackendOnDataEmbeddingProfile() {
        propertiesOnlyRunner
            .withPropertyValues(
                "mill.ai.data.embedding.default.vector-store.backend=pgvector",
                "mill.ai.data.embedding.default.vector-store.pgvector.table=custom_lc_store",
                "mill.ai.data.embedding.default.vector-store.pgvector.create-table=false",
                "mill.ai.data.embedding.default.vector-store.pgvector.use-index=true",
                "mill.ai.data.embedding.default.vector-store.pgvector.index-list-size=64",
            )
            .run { ctx ->
                val pg = ctx.getBean(DataEmbeddingConfigurationProperties::class.java)
                    .embedding["default"]!!.vectorStore.pgvector
                assertThat(pg.table).isEqualTo("custom_lc_store")
                assertThat(pg.isCreateTable).isFalse()
                assertThat(pg.isUseIndex).isTrue()
                assertThat(pg.indexListSize).isEqualTo(64)
            }
    }

    @Test
    fun shouldMergeRegistryVectorStoreWithProfilePgVectorTableOverride() {
        propertiesOnlyRunner
            .withPropertyValues(
                "mill.ai.vector-stores.pg.backend=pgvector",
                "mill.ai.vector-stores.pg.pgvector.table=registry_table",
                "mill.ai.data.embedding.default.vector-store.backend=pg",
                "mill.ai.data.embedding.default.vector-store.pgvector.table=profile_table",
            )
            .run { ctx ->
                val root = ctx.getBean(AiConfigurationProperties::class.java)
                val profile = ctx.getBean(DataEmbeddingConfigurationProperties::class.java).embedding["default"]!!
                val effective = VectorStoreConfigMerger(root).resolve(profile.vectorStore)
                assertThat(effective.backend()).isEqualTo(VectorStoreSettings.Backend.PGVECTOR)
                assertThat(effective.pgvector().table).isEqualTo("profile_table")
            }
    }

    @Test
    fun shouldFailWhenPgVectorBackendWithoutDataSource() {
        embeddingAndVectorRunner
            .withPropertyValues(
                *MillAiTestProperties.minimalAiStack(),
                "mill.ai.data.embedding.default.vector-store.backend=pgvector",
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
                *MillAiTestProperties.minimalAiStack(),
                "mill.ai.data.embedding.default.vector-store.backend=pgvector",
            )
            .run { ctx ->
                assertThat(ctx).hasFailed()
                assertThat(ctx.getStartupFailure()!!.cause)
                    .hasMessageContaining("PostgreSQL")
            }
    }

    @Configuration
    @EnableConfigurationProperties(
        AiConfigurationProperties::class,
        DataEmbeddingConfigurationProperties::class,
    )
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
