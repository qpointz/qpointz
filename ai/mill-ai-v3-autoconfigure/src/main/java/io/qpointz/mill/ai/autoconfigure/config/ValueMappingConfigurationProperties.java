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

        private final OnStartup onStartup = new OnStartup();
        private final Schedule schedule = new Schedule();

        public OnStartup getOnStartup() {
            return onStartup;
        }

        public Schedule getSchedule() {
            return schedule;
        }

        /**
         * {@code mill.ai.value-mapping.refresh.on-startup.*}.
         */
        public static class OnStartup {

            /**
             * When {@code false}, no {@code APP_STARTUP} refresh runs.
             */
            private boolean enabled = true;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        /**
         * {@code mill.ai.value-mapping.refresh.schedule.*}.
         */
        public static class Schedule {

            /**
             * When {@code false}, the scheduled refresh job is not registered.
             */
            private boolean enabled = true;

            /**
             * Cadence for scheduled job wake/evaluate (not per-facet {@code refreshInterval}).
             */
            private Duration interval = Duration.ofMinutes(15);

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Duration getInterval() {
                return interval;
            }

            public void setInterval(Duration interval) {
                this.interval = interval;
            }
        }
    }
}
