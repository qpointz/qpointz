package io.qpointz.mill.ai.autoconfigure.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Root {@code mill.ai.*} bindings: providers, named models, and optional shared vector-store registry.
 *
 * <p>Example YAML:
 * <pre>{@code
 * mill:
 *   ai:
 *     enabled: true
 *     providers:
 *       openai:
 *         type: openai
 *         api-key: ${OPENAI_API_KEY}
 *         base-url: https://api.openai.com/v1
 *     models:
 *       chat:
 *         default:
 *           provider: openai
 *           model-name: gpt-4o-mini
 *       embedding:
 *         default:
 *           provider: openai
 *           model-name: text-embedding-3-small
 *           dimension: 1536
 *     vector-stores:
 *       pg:
 *         backend: pgvector
 * }</pre>
 */
@ConfigurationProperties(prefix = "mill.ai")
public class AiConfigurationProperties {

    private boolean enabled = true;

    private Map<String, AiProviderEntry> providers = new LinkedHashMap<>();

    private final Models models = new Models();

    private Map<String, VectorStoreSettings.Connection> vectorStores = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, AiProviderEntry> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, AiProviderEntry> providers) {
        this.providers = providers != null ? providers : new LinkedHashMap<>();
    }

    public Models getModels() {
        return models;
    }

    public Map<String, VectorStoreSettings.Connection> getVectorStores() {
        return vectorStores;
    }

    public void setVectorStores(Map<String, VectorStoreSettings.Connection> vectorStores) {
        this.vectorStores = vectorStores != null ? vectorStores : new LinkedHashMap<>();
    }

    /**
     * Named chat and embedding model profiles.
     */
    public static class Models {

        private Map<String, ChatModelProfile> chat = new LinkedHashMap<>();

        private Map<String, EmbeddingModelProfile> embedding = new LinkedHashMap<>();

        public Map<String, ChatModelProfile> getChat() {
            return chat;
        }

        public void setChat(Map<String, ChatModelProfile> chat) {
            this.chat = chat != null ? chat : new LinkedHashMap<>();
        }

        public Map<String, EmbeddingModelProfile> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(Map<String, EmbeddingModelProfile> embedding) {
            this.embedding = embedding != null ? embedding : new LinkedHashMap<>();
        }
    }

    /**
     * Single provider entry: type, shared secret, and HTTP settings.
     */
    public static class AiProviderEntry {

        private String type = "openai";

        private String apiKey;

        private String baseUrl;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type != null ? type : "openai";
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    /**
     * Non-secret chat model profile referencing a {@link AiProviderEntry} id.
     */
    public static class ChatModelProfile {

        private String provider = "openai";

        private String modelName = "gpt-4o-mini";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }
    }

    /**
     * Non-secret embedding profile: references a {@link AiProviderEntry} id when using a remote provider.
     */
    public static class EmbeddingModelProfile {

        private String provider = "stub";

        private String modelName = "text-embedding-3-small";

        private int dimension = 384;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }
    }
}
