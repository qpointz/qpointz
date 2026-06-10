package io.qpointz.mill.ai.autoconfigure.config;

import java.time.Duration;
import java.util.Locale;

/**
 * Shared vector-store backend settings for {@code mill.ai.vector-stores.*} and per-profile
 * {@code mill.ai.data.embedding.<profile>.vector-store.*}.
 */
public final class VectorStoreSettings {

    private VectorStoreSettings() {}

    /**
     * Backend implementation id: built-in {@link Backend} name or a key into {@code mill.ai.vector-stores}.
     */
    public enum Backend {
        IN_MEMORY,
        CHROMA,
        PGVECTOR;

        /**
         * @param value raw {@code backend} property
         * @return built-in backend when {@code value} matches a known id (case-insensitive), else {@code null}
         */
        public static Backend fromConfigValue(String value) {
            if (value == null || value.isBlank()) {
                return IN_MEMORY;
            }
            String normalized = value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
            for (Backend backend : values()) {
                if (backend.name().equalsIgnoreCase(normalized.replace('_', '-'))
                        || backend.name().replace('_', '-').equalsIgnoreCase(normalized)) {
                    return backend;
                }
            }
            if ("in_memory".equals(normalized) || "inmemory".equals(normalized)) {
                return IN_MEMORY;
            }
            if ("pgvector".equals(normalized) || "pg_vector".equals(normalized)) {
                return PGVECTOR;
            }
            return null;
        }
    }

    /**
     * Registry entry under {@code mill.ai.vector-stores.<id>}.
     */
    public static class Connection {

        private Backend backend = Backend.IN_MEMORY;

        private Chroma chroma = new Chroma();

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
    }

    /**
     * Per-profile vector store: {@code backend} may be a built-in id or a {@code mill.ai.vector-stores} key.
     */
    public static class Profile {

        private String backend = "in-memory";

        private Chroma chroma = new Chroma();

        private PgVector pgvector = new PgVector();

        public String getBackend() {
            return backend;
        }

        public void setBackend(String backend) {
            this.backend = backend != null ? backend : "in-memory";
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
    }

    /**
     * Non-secret Chroma HTTP client settings.
     */
    public static class Chroma {

        private String baseUrl;

        private ApiVersion apiVersion = ApiVersion.V2;

        private String tenantName = "default_tenant";

        private String databaseName = "default_database";

        private String collectionName = "mill-value-mapping";

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

        public enum ApiVersion {
            V1,
            V2
        }
    }

    /**
     * Non-secret pgvector / LangChain4j {@code PgVectorEmbeddingStore} settings.
     */
    public static class PgVector {

        private String table = "mill_langchain_embedding_store";

        private boolean createTable = true;

        private boolean useIndex = false;

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

    /**
     * Resolved backend + merged Chroma/pgvector settings for factory wiring.
     */
    public record Effective(
            Backend backend,
            Chroma chroma,
            PgVector pgvector
    ) {}
}
