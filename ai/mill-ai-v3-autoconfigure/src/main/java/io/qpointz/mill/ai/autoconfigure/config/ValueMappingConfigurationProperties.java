package io.qpointz.mill.ai.autoconfigure.config;

import java.time.Duration;

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

    /**
     * Maximum {@link String#length()} for {@code AttributeValueEntry.content} before embed (WI-181).
     */
    private int maxContentLength = 2048;

    private final Refresh refresh = new Refresh();

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public Refresh getRefresh() {
        return refresh;
    }

    /**
     * {@code mill.ai.value-mapping.refresh.*} (WI-182).
     */
    public static class Refresh {

        /**
         * When {@code false}, no {@code APP_STARTUP} refresh runs.
         */
        private boolean startupEnabled = true;

        /**
         * When {@code true}, scheduled ticks are disabled.
         */
        private boolean scheduledDisabled = false;

        /**
         * Cadence for scheduled job wake/evaluate (not per-facet {@code refreshInterval}).
         */
        private Duration scheduleInterval = Duration.ofMinutes(15);

        public boolean isStartupEnabled() {
            return startupEnabled;
        }

        public void setStartupEnabled(boolean startupEnabled) {
            this.startupEnabled = startupEnabled;
        }

        public boolean isScheduledDisabled() {
            return scheduledDisabled;
        }

        public void setScheduledDisabled(boolean scheduledDisabled) {
            this.scheduledDisabled = scheduledDisabled;
        }

        public Duration getScheduleInterval() {
            return scheduleInterval;
        }

        public void setScheduleInterval(Duration scheduleInterval) {
            this.scheduleInterval = scheduleInterval;
        }
    }
}
