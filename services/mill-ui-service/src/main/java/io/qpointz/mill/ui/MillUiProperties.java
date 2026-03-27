package io.qpointz.mill.ui;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the embedded Mill UI: static resources under {@code classpath:/static/app/{version}/}
 * and servlet filter routing for SPA deep-links.
 */
@ConfigurationProperties(prefix = "mill.ui")
public class MillUiProperties {

    /**
     * When {@code true}, registers the SPA resource handler and routing filter. When {@code false},
     * no Mill UI beans are created.
     */
    private boolean enabled = true;

    /**
     * Packaged UI build directory name under {@code classpath:/static/app/} (e.g. {@code v1}, {@code v2}).
     */
    private String version = "v2";

    /**
     * URL path prefix for the SPA, without trailing slash. Must match the Vite {@code base} setting
     * (typically {@code /app}).
     */
    private String appBasePath = "/app";

    /**
     * Request path forwarded by the SPA filter for deep-links (usually {@code {appBasePath}/index.html}).
     */
    private String spaIndexPath = "/app/index.html";

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled whether Mill UI beans are active
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVersion() {
        return version;
    }

    /**
     * @param version subdirectory under {@code classpath:/static/app/} for static files
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppBasePath() {
        return appBasePath;
    }

    /**
     * @param appBasePath public path prefix for the UI (e.g. {@code /app})
     */
    public void setAppBasePath(String appBasePath) {
        this.appBasePath = appBasePath;
    }

    public String getSpaIndexPath() {
        return spaIndexPath;
    }

    /**
     * @param spaIndexPath path used when forwarding SPA routes to {@code index.html}
     */
    public void setSpaIndexPath(String spaIndexPath) {
        this.spaIndexPath = spaIndexPath;
    }
}
