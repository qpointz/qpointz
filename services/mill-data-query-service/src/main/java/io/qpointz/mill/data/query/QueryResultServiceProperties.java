package io.qpointz.mill.data.query;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindings for {@code mill.data.services.query.*} (parity with {@code mill.data.services.http}
 * and {@code mill.data.services.export}).
 */
@ConditionalOnService(value = "query", group = "data")
@ConfigurationProperties(prefix = "mill.data.services.query")
public class QueryResultServiceProperties {

    /** When {@code false}, query-result MVC beans and descriptors do not register. */
    private Boolean enable;

    /**
     * Logical key into {@code mill.application.hosts.externals} for discovery URLs
     * (same pattern as the HTTP data-plane and export services).
     */
    private String externalHost = "";

    /**
     * @return whether the query-result HTTP surface is enabled
     */
    public Boolean getEnable() {
        return enable;
    }

    /**
     * @param enable whether the query-result HTTP surface is enabled
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
}
