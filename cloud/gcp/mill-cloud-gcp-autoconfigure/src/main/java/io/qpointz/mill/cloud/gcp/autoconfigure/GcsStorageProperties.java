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

    /**
     * Optional GCS JSON API base URL for emulators (for example {@code http://127.0.0.1:4443} for fake-gcs-server).
     * When set, the client uses {@link com.google.cloud.NoCredentials} against that host.
     */
    private String emulatorHost;

    /**
     * Optional Google Cloud project id (recommended when using {@link #emulatorHost}).
     */
    private String projectId;

    /**
     * @return optional emulator base URL, or {@code null} for production Google Cloud Storage
     */
    public String getEmulatorHost() {
        return emulatorHost;
    }

    /**
     * @param emulatorHost optional emulator base URL
     */
    public void setEmulatorHost(String emulatorHost) {
        this.emulatorHost = emulatorHost;
    }

    /**
     * @return optional project id override
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * @param projectId optional project id
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
