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

    /**
     * Azure Storage connection string (Azurite or production). Used by {@code azure-blob://} Spring resources when set.
     */
    private String connectionString;

    /**
     * Blob service endpoint URL when not using {@link #connectionString} (for example {@code https://{account}.blob.core.windows.net}).
     * Paired with ambient credentials from the Azure SDK default chain.
     */
    private String blobServiceEndpoint;

    /**
     * @return optional connection string
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * @param connectionString optional connection string
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * @return optional blob service endpoint URL
     */
    public String getBlobServiceEndpoint() {
        return blobServiceEndpoint;
    }

    /**
     * @param blobServiceEndpoint optional blob service endpoint
     */
    public void setBlobServiceEndpoint(String blobServiceEndpoint) {
        this.blobServiceEndpoint = blobServiceEndpoint;
    }
}
