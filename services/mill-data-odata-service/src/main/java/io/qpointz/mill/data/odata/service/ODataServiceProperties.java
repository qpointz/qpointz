package io.qpointz.mill.data.odata.service;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

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

    /** OData metadata caching (Caffeine). */
    private CacheProperties cache = new CacheProperties();

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

    /**
     * @return OData metadata cache settings
     */
    public CacheProperties getCache() {
        return cache;
    }

    /**
     * @param cache OData metadata cache settings
     */
    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    @Getter
    @Setter
    public static class CacheProperties {

        /** EDM and schema-facet snapshot cache for OData reads and {@code $metadata}. */
        private EdmCacheProperties edm = new EdmCacheProperties();

        /**
         * @return EDM cache settings
         */
        public EdmCacheProperties getEdm() {
            return edm;
        }

        /**
         * @param edm EDM cache settings
         */
        public void setEdm(EdmCacheProperties edm) {
            this.edm = edm;
        }
    }

    @Getter
    @Setter
    public static class EdmCacheProperties {

        /**
         * When {@code false}, EDM and table facet metadata are resolved on every request (no Caffeine).
         */
        private boolean enabled = false;

        /**
         * Optional EDM cache TTL (for example {@code 2m}, {@code PT2M}).
         * When unset, entries do not expire by time.
         */
        private Duration ttl;

        /**
         * @return whether EDM and table facet caching is active
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @param enabled whether EDM and table facet caching is active
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * @return optional cache TTL
         */
        public Duration getTtl() {
            return ttl;
        }

        /**
         * @param ttl optional cache TTL
         */
        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}