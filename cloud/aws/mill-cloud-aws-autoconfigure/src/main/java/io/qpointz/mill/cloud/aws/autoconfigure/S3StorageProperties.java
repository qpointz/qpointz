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

    /**
     * Optional S3 endpoint override (for example MinIO or LocalStack).
     */
    private String endpoint;

    /**
     * Optional AWS region when not implied by the default provider chain.
     */
    private String region;

    /**
     * @return optional endpoint override URL
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint optional S3-compatible endpoint URL
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return optional region id
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region optional AWS region id
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Optional access key for static credentials (for example MinIO or explicit keys alongside a custom endpoint).
     */
    private String accessKey;

    /**
     * Optional secret key paired with {@link #accessKey}.
     */
    private String secretKey;

    /**
     * @return optional access key for static credentials
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @param accessKey optional access key
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @return optional secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey optional secret key
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
