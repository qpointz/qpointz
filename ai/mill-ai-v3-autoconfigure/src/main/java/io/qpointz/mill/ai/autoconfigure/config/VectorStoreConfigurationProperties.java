package io.qpointz.mill.ai.autoconfigure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Runtime vector store selection under {@code mill.ai.vector-store.*}.
 */
@ConfigurationProperties(prefix = "mill.ai.vector-store")
public class VectorStoreConfigurationProperties {

    /**
     * Backend implementation. MVP: {@code in-memory} only; pgvector/Chroma attach here later.
     */
    private Backend backend = Backend.IN_MEMORY;

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend != null ? backend : Backend.IN_MEMORY;
    }

    public enum Backend {
        /** LangChain4j in-memory store (default CI / dev). */
        IN_MEMORY
    }
}
