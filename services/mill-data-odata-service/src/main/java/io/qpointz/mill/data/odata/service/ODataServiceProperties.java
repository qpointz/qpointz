package io.qpointz.mill.data.odata.service;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindings for {@code mill.data.services.odata.*} (parity with export / query data-plane services).
 */
@ConditionalOnService(value = "odata", group = "data")
@ConfigurationProperties(prefix = "mill.data.services.odata")
public class ODataServiceProperties {

    /** When {@code false}, OData MVC beans and descriptors do not register. */
    private Boolean enable;

    /** Logical key into {@code mill.application.hosts.externals}. */
    private String externalHost = "";

    /** Optional metadata facet scope (reserved; global when blank). */
    private String defaultScope = "";

    /** Maximum {@code $top} accepted on entity reads. */
    private int maxTop = 10_000;

    /**
     * @return whether the OData HTTP surface is enabled
     */
    public Boolean getEnable() {
        return enable;
    }

    /**
     * @param enable whether the OData HTTP surface is enabled
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * @return logical key into {@code mill.application.hosts.externals}
     */
    public String getExternalHost() {
        return externalHost;
    }

    /**
     * @param externalHost logical key into {@code mill.application.hosts.externals}
     */
    public void setExternalHost(String externalHost) {
        this.externalHost = externalHost;
    }

    /**
     * @return optional metadata facet scope (reserved)
     */
    public String getDefaultScope() {
        return defaultScope;
    }

    /**
     * @param defaultScope optional metadata facet scope
     */
    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }

    /**
     * @return maximum {@code $top} accepted on entity reads
     */
    public int getMaxTop() {
        return maxTop;
    }

    /**
     * @param maxTop maximum {@code $top} accepted on entity reads
     */
    public void setMaxTop(int maxTop) {
        this.maxTop = maxTop;
    }
}