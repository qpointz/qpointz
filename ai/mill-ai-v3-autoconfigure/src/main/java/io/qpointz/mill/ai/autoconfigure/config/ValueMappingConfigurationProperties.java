package io.qpointz.mill.ai.autoconfigure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Value-mapping feature keys under {@code mill.ai.value-mapping.*}.
 */
@ConfigurationProperties(prefix = "mill.ai.value-mapping")
public class ValueMappingConfigurationProperties {

    /**
     * Key into {@code mill.ai.embedding-model.*} selecting the active embedding profile.
     */
    private String embeddingModel = "default";

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
