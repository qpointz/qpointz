package io.qpointz.mill.ai.autoconfigure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Runtime vector store selection under {@code mill.ai.vector-store.*}.
 */
@ConfigurationProperties(prefix = "mill.ai.vector-store")
public class VectorStoreConfigurationProperties {

    /**
     * Backend implementation: {@code in-memory} (default), {@code chroma} (LangChain4j HTTP client), or
     * {@code pgvector} (LangChain4j {@link dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore} on the
     * primary {@link javax.sql.DataSource}).
     */
    private Backend backend = Backend.IN_MEMORY;

    /**
     * Settings used when {@link #getBackend()} is {@link Backend#CHROMA}.
     */
    private Chroma chroma = new Chroma();

    /**
     * Settings used when {@link #getBackend()} is {@link Backend#PGVECTOR}.
     * <p>
     * This table is <strong>only</strong> for LangChain4j ANN search. Authoritative value-mapping rows and bytes remain
     * in {@code ai_value_mapping.embedding} ({@code BYTEA}) per WI-174; do not point this store at that column.
     */
    private PgVector pgvector = new PgVector();

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend != null ? backend : Backend.IN_MEMORY;
    }

    public Chroma getChroma() {
        return chroma;
    }

    public void setChroma(Chroma chroma) {
        this.chroma = chroma != null ? chroma : new Chroma();
    }

    public PgVector getPgvector() {
        return pgvector;
    }

    public void setPgvector(PgVector pgvector) {
        this.pgvector = pgvector != null ? pgvector : new PgVector();
    }

    public enum Backend {
        /** LangChain4j in-memory store (default CI / dev). */
        IN_MEMORY,
        /** LangChain4j {@link dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore} (HTTP). */
        CHROMA,
        /** LangChain4j {@link dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore} (PostgreSQL + {@code vector} extension). */
        PGVECTOR
    }

    /**
     * Non-secret Chroma HTTP client settings (see LangChain4j Chroma integration).
     */
    public static class Chroma {

        /**
         * Chroma HTTP API base URL (no trailing slash), for example {@code http://localhost:8000}.
         * Required when {@link VectorStoreConfigurationProperties#getBackend()} is {@link Backend#CHROMA}.
         */
        private String baseUrl;

        /**
         * Chroma REST API version exposed by the server ({@code V2} is typical for current Chroma deployments).
         */
        private ApiVersion apiVersion = ApiVersion.V2;

        /**
         * Tenant name for Chroma v2 multi-tenant layout.
         */
        private String tenantName = "default_tenant";

        /**
         * Logical database name within the tenant.
         */
        private String databaseName = "default_database";

        /**
         * Collection that holds embedded segments for value-mapping / search workloads.
         */
        private String collectionName = "mill-value-mapping";

        /**
         * HTTP client timeout for Chroma requests.
         */
        private Duration timeout = Duration.ofSeconds(60);

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public ApiVersion getApiVersion() {
            return apiVersion;
        }

        public void setApiVersion(ApiVersion apiVersion) {
            this.apiVersion = apiVersion != null ? apiVersion : ApiVersion.V2;
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getCollectionName() {
            return collectionName;
        }

        public void setCollectionName(String collectionName) {
            this.collectionName = collectionName;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout != null ? timeout : Duration.ofSeconds(60);
        }

        /**
         * Chroma HTTP API variant supported by LangChain4j.
         */
        public enum ApiVersion {
            V1,
            V2
        }
    }

    /**
     * Non-secret pgvector / LangChain4j {@code PgVectorEmbeddingStore} settings (PostgreSQL {@code vector} type).
     * Embedding vector dimension is taken from {@link io.qpointz.mill.ai.embedding.EmbeddingHarness#dimension()}, not
     * from this group.
     */
    public static class PgVector {

        /**
         * Table for LangChain4j embedding rows (separate from {@code ai_value_mapping}).
         */
        private String table = "mill_langchain_embedding_store";

        /**
         * When {@code true}, {@code PgVectorEmbeddingStore} creates the table if missing (requires {@code vector}
         * extension and suitable privileges).
         */
        private boolean createTable = true;

        /**
         * When {@code true}, request an IVFFlat index via LangChain4j (large corpora); {@link #indexListSize} must be
         * set &gt; 0.
         */
        private boolean useIndex = false;

        /**
         * IVFFlat {@code lists} parameter; used only when {@link #useIndex} is {@code true}.
         */
        private Integer indexListSize;

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public boolean isCreateTable() {
            return createTable;
        }

        public void setCreateTable(boolean createTable) {
            this.createTable = createTable;
        }

        public boolean isUseIndex() {
            return useIndex;
        }

        public void setUseIndex(boolean useIndex) {
            this.useIndex = useIndex;
        }

        public Integer getIndexListSize() {
            return indexListSize;
        }

        public void setIndexListSize(Integer indexListSize) {
            this.indexListSize = indexListSize;
        }
    }
}
