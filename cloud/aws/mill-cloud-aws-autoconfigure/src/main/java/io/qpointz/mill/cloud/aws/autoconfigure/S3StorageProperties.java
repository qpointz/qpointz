package io.qpointz.mill.cloud.aws.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Mill AWS S3 storage integration.
 *
 * <p>Bound to the {@code mill.cloud.aws.s3} prefix.
 *
 * <p>Example YAML:
 * <pre>{@code
 * mill:
 *   cloud:
 *     aws:
 *       s3:
 *         enabled: true
 * }</pre>
 */
@ConfigurationProperties(prefix = "mill.cloud.aws.s3")
public class S3StorageProperties {

    /**
     * Whether S3 storage autoconfiguration is enabled.
     * Defaults to {@code true}.
     */
    private boolean enabled = true;

    /**
     * Returns whether S3 storage autoconfiguration is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether S3 storage autoconfiguration is enabled.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
