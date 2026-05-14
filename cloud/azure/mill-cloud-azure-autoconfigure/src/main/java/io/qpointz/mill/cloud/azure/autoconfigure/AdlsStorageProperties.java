package io.qpointz.mill.cloud.azure.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Azure Data Lake Storage / Blob Storage auto-configuration.
 *
 * <p>Bound to {@code mill.cloud.azure.adls.*} in {@code application.yml} /
 * {@code application.properties}.
 *
 * <pre>{@code
 * mill:
 *   cloud:
 *     azure:
 *       adls:
 *         enabled: true
 * }</pre>
 */
@ConfigurationProperties(prefix = "mill.cloud.azure.adls")
public class AdlsStorageProperties {

    /** When {@code true}, registers the ADLS {@link io.qpointz.mill.source.factory.StorageFactory} bean. */
    private boolean enabled = true;

    /**
     * @return whether the ADLS storage factory bean is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled whether the ADLS storage factory bean is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
