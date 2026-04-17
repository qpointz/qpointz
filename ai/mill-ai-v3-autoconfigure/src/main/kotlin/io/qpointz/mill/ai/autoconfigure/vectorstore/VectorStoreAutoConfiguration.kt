package io.qpointz.mill.ai.autoconfigure.vectorstore

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.config.VectorStoreConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

/**
 * Single [EmbeddingStore] bean per application: in-memory (default), Chroma HTTP ([ChromaEmbeddingStore]), or
 * PostgreSQL pgvector ([PgVectorEmbeddingStore]).
 */
@ConditionalOnAiEnabled
@AutoConfiguration(after = [EmbeddingAutoConfiguration::class])
@EnableConfigurationProperties(VectorStoreConfigurationProperties::class)
class VectorStoreAutoConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnMissingBean(EmbeddingStore::class)
    fun embeddingStore(
        props: VectorStoreConfigurationProperties,
        embeddingHarnessProvider: ObjectProvider<EmbeddingHarness>,
        dataSourceProvider: ObjectProvider<DataSource>,
    ): EmbeddingStore<TextSegment> {
        log.info("Vector store backend={}", props.backend)
        return when (props.backend) {
            VectorStoreConfigurationProperties.Backend.IN_MEMORY ->
                InMemoryEmbeddingStore()
            VectorStoreConfigurationProperties.Backend.CHROMA ->
                chromaEmbeddingStore(props)
            VectorStoreConfigurationProperties.Backend.PGVECTOR ->
                pgVectorEmbeddingStore(props, embeddingHarnessProvider, dataSourceProvider)
        }
    }

    private fun chromaEmbeddingStore(props: VectorStoreConfigurationProperties): EmbeddingStore<TextSegment> {
        val c = props.chroma
        val baseUrl = c.baseUrl?.trim().orEmpty()
        if (baseUrl.isEmpty()) {
            throw IllegalStateException(
                "mill.ai.vector-store.chroma.base-url is required when mill.ai.vector-store.backend is chroma",
            )
        }
        val apiVersion = when (c.apiVersion) {
            VectorStoreConfigurationProperties.Chroma.ApiVersion.V1 -> ChromaApiVersion.V1
            VectorStoreConfigurationProperties.Chroma.ApiVersion.V2 -> ChromaApiVersion.V2
        }
        log.info(
            "Chroma vector store baseUrl={} apiVersion={} collection={}",
            baseUrl,
            apiVersion,
            c.collectionName,
        )
        return ChromaEmbeddingStore.builder()
            .apiVersion(apiVersion)
            .baseUrl(baseUrl)
            .tenantName(c.tenantName)
            .databaseName(c.databaseName)
            .collectionName(c.collectionName)
            .timeout(c.timeout)
            .build()
    }

    private fun pgVectorEmbeddingStore(
        props: VectorStoreConfigurationProperties,
        embeddingHarnessProvider: ObjectProvider<EmbeddingHarness>,
        dataSourceProvider: ObjectProvider<DataSource>,
    ): EmbeddingStore<TextSegment> {
        val dataSource = dataSourceProvider.getIfAvailable()
            ?: throw IllegalStateException(
                "mill.ai.vector-store.backend=pgvector requires a javax.sql.DataSource bean (PostgreSQL). " +
                    "For H2 or Postgres without pgvector, use mill.ai.vector-store.backend=in-memory or chroma.",
            )
        val harness = embeddingHarnessProvider.getIfAvailable()
            ?: throw IllegalStateException(
                "mill.ai.vector-store.backend=pgvector requires an EmbeddingHarness bean " +
                    "(configure mill.ai.embedding-model and mill.ai.value-mapping.embedding-model).",
            )
        assertPostgreSqlWithVectorExtension(dataSource)
        val pg = props.pgvector
        val table = pg.table?.trim().orEmpty()
        if (table.isEmpty()) {
            throw IllegalStateException("mill.ai.vector-store.pgvector.table must not be blank when backend is pgvector")
        }
        val dim = harness.dimension
        log.info(
            "PgVector embedding store table={} dimension={} createTable={} useIndex={}",
            table,
            dim,
            pg.isCreateTable,
            pg.isUseIndex,
        )
        val datasourceBuilder = PgVectorEmbeddingStore.datasourceBuilder()
            .datasource(dataSource)
            .table(table)
            .dimension(dim)
            .createTable(pg.isCreateTable)
        val configuredBuilder = if (pg.isUseIndex) {
            val listSize = pg.indexListSize
                ?: throw IllegalStateException(
                    "mill.ai.vector-store.pgvector.index-list-size is required when mill.ai.vector-store.pgvector.use-index is true",
                )
            if (listSize <= 0) {
                throw IllegalStateException(
                    "mill.ai.vector-store.pgvector.index-list-size must be greater than zero when use-index is true",
                )
            }
            datasourceBuilder.useIndex(true).indexListSize(listSize)
        } else {
            datasourceBuilder
        }
        return configuredBuilder.build()
    }

    private fun assertPostgreSqlWithVectorExtension(dataSource: DataSource) {
        dataSource.connection.use { conn ->
            val product = conn.metaData.databaseProductName.lowercase()
            if (!product.contains("postgresql")) {
                throw IllegalStateException(
                    "mill.ai.vector-store.backend=pgvector requires a PostgreSQL DataSource " +
                        "(found database product '$product'). Use in-memory or chroma for non-PostgreSQL databases.",
                )
            }
            conn.createStatement().use { st ->
                st.executeQuery(
                    "SELECT 1 FROM pg_extension WHERE extname = 'vector' LIMIT 1",
                ).use { rs ->
                    if (!rs.next()) {
                        throw IllegalStateException(
                            "mill.ai.vector-store.backend=pgvector requires the PostgreSQL 'vector' extension " +
                                "(pgvector). Create it with CREATE EXTENSION IF NOT EXISTS vector; on this database, " +
                                "or use mill.ai.vector-store.backend=in-memory or chroma.",
                        )
                    }
                }
            }
        }
    }
}
