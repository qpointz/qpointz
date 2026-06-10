package io.qpointz.mill.ai.autoconfigure.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Named embedding data pipelines under {@code mill.ai.data.embedding.<profile>.*}.
 */
@ConfigurationProperties(prefix = "mill.ai.data")
public class DataEmbeddingConfigurationProperties {

    private Map<String, EmbeddingDataProfile> embedding = new LinkedHashMap<>();

    public Map<String, EmbeddingDataProfile> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Map<String, EmbeddingDataProfile> embedding) {
        this.embedding = embedding != null ? embedding : new LinkedHashMap<>();
    }

    /**
     * Single embedding pipeline profile: model ref, vector store, refresh ops, and data sources.
     */
    public static class EmbeddingDataProfile {

        /**
         * Key into {@code mill.ai.models.embedding.*}.
         */
        private String model = "default";

        private final VectorStoreSettings.Profile vectorStore = new VectorStoreSettings.Profile();

        private int maxContentLength = 2048;

        private final RefreshSettings refresh = new RefreshSettings();

        private List<EmbeddingSource> sources = new ArrayList<>();

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public VectorStoreSettings.Profile getVectorStore() {
            return vectorStore;
        }

        public int getMaxContentLength() {
            return maxContentLength;
        }

        public void setMaxContentLength(int maxContentLength) {
            this.maxContentLength = maxContentLength;
        }

        public RefreshSettings getRefresh() {
            return refresh;
        }

        public List<EmbeddingSource> getSources() {
            return sources;
        }

        public void setSources(List<EmbeddingSource> sources) {
            this.sources = sources != null ? sources : new ArrayList<>();
        }
    }

    /**
     * Data acquisition source; v1 supports {@code metadata-facets} only.
     */
    public static class EmbeddingSource {

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
