package io.qpointz.mill.events.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Mill event bus.
 *
 * <p>Prefix: {@code mill.events}
 */
@ConfigurationProperties(prefix = "mill.events")
public class EventsProperties {

    /**
     * Transport implementation to use.
     * <p>Supported values: {@code in-memory}, {@code spring}.
     */
    private String transport = "in-memory";

    /**
     * Nested publish properties.
     */
    private Publish publish = new Publish();

    /**
     * Nested async properties.
     */
    private Async async = new Async();

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public Publish getPublish() {
        return publish;
    }

    public void setPublish(Publish publish) {
        this.publish = publish;
    }

    public Async getAsync() {
        return async;
    }

    public void setAsync(Async async) {
        this.async = async;
    }

    /**
     * Publish-related properties.
     */
    public static class Publish {

        /**
         * Default publish mode.
         * <p>Supported values: {@code async}, {@code sync}.
         */
        private String mode = "async";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    /**
     * Async dispatch properties.
     */
    public static class Async {

        /**
         * Whether async dispatch is enabled. Set to {@code false} for deterministic test execution.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
