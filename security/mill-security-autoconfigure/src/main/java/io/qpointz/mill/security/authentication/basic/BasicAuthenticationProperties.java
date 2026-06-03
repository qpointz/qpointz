package io.qpointz.mill.security.authentication.basic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code mill.security.authentication.basic.*} for HTTP Basic password authentication.
 */
@ConfigurationProperties(prefix = "mill.security.authentication.basic")
public class BasicAuthenticationProperties {

    /**
     * Whether HTTP Basic authentication is registered in the security filter chain.
     */
    private boolean enable;

    /**
     * Credential store selector: {@code jpa} for database-backed users, or a Spring resource
     * location (e.g. {@code file:./config/auth.yml}, {@code classpath:passwd.yml}).
     */
    private String store;

    /**
     * @deprecated use {@link #store} instead; still honored when {@code store} is unset
     */
    @Deprecated
    private String fileStore;

    /**
     * @return whether basic authentication is enabled
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * @param enable whether basic authentication is enabled
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * @return store selector ({@code jpa} or resource path)
     */
    public String getStore() {
        if (store != null && !store.isBlank()) {
            return store.strip();
        }
        return fileStore == null ? "" : fileStore.strip();
    }

    /**
     * @param store {@code jpa} or a resource path for the YAML user store
     */
    public void setStore(String store) {
        this.store = store;
    }

    /**
     * @return legacy {@code file-store} property value
     * @deprecated use {@link #setStore(String)}
     */
    @Deprecated
    public String getFileStore() {
        return fileStore;
    }

    /**
     * @param fileStore legacy resource path
     * @deprecated use {@link #setStore(String)}
     */
    @Deprecated
    public void setFileStore(String fileStore) {
        this.fileStore = fileStore;
    }
}
