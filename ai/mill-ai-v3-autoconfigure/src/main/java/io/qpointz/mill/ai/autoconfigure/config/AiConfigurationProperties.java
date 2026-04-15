package io.qpointz.mill.ai.autoconfigure.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Root {@code mill.ai.*} bindings: providers and named embedding profiles.
 *
 * <p>Example YAML:
 * <pre>{@code
 * mill:
 *   ai:
 *     enabled: true
 *     providers:
 *       openai:
 *         api-key: ${OPENAI_API_KEY}
 *         base-url: https://api.openai.com/v1
 *     embedding-model:
 *       default:
 *         provider: stub
 *         dimension: 384
 *       openai-small:
 *         provider: openai
 *         model-name: text-embedding-3-small
 *         dimension: 1536
 * }</pre>
 */
@ConfigurationProperties(prefix = "mill.ai")
public class AiConfigurationProperties {

    /**
     * When {@code false}, mill-ai-v3-autoconfigure skips registering AI beans (providers,
     * embedding harness, vector store, chat runtime, data/schema helpers, etc.). Defaults to
     * {@code true}. Bindings under {@code mill.ai.*} still apply for documentation and tooling when
     * this class is registered.
     */
    private boolean enabled = true;

    private Map<String, AiProviderEntry> providers = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Named embedding profiles (referenced by {@code mill.ai.value-mapping.embedding-model}).
     */
    private Map<String, EmbeddingModelProfile> embeddingModel = new LinkedHashMap<>();

    public Map<String, AiProviderEntry> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, AiProviderEntry> providers) {
        this.providers = providers != null ? providers : new LinkedHashMap<>();
    }

    public Map<String, EmbeddingModelProfile> getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(Map<String, EmbeddingModelProfile> embeddingModel) {
        this.embeddingModel = embeddingModel != null ? embeddingModel : new LinkedHashMap<>();
    }

    /**
     * Single provider entry: shared secret and HTTP settings.
     */
    public static class AiProviderEntry {

        private String apiKey;

        private String baseUrl;

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
     * Non-secret embedding profile: references a {@link AiProviderEntry} id when using a remote provider.
     */
    public static class EmbeddingModelProfile {

        /**
         * Provider id ({@code openai}) or {@code stub} for deterministic local embeddings.
         */
        private String provider = "stub";

        /**
         * Remote model name when {@link #provider} is not {@code stub}.
         */
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
