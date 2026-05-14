package io.qpointz.mill.cloud.gcp.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for the GCS blob storage integration.
 *
 * <p>Bind to {@code mill.cloud.gcp.gcs.*} in YAML / properties files:
 * <pre>{@code
 * mill:
 *   cloud:
 *     gcp:
 *       gcs:
 *         enabled: true
 * }</pre>
 */
@ConfigurationProperties(prefix = "mill.cloud.gcp.gcs")
public class GcsStorageProperties {

    /**
     * Whether the GCS storage auto-configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * Returns whether the GCS storage auto-configuration is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the GCS storage auto-configuration is enabled.
     *
     * @param enabled {@code true} to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
