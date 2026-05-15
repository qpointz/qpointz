package io.qpointz.mill.autoconfigure.data.backend.flow;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

@Getter
@Setter
@ConfigurationProperties(prefix = MILL_DATA_BACKEND_CONFIG_KEY + ".flow")
public class FlowBackendProperties {

    /**
     * Mill resource locations for flow source descriptor YAML (for example {@code classpath:},
     * {@code file:}, bare paths, or cloud URIs when resolvers are registered).
     */
    private List<String> sources = new ArrayList<>();

    /**
     * Caching configuration for the flow backend.
     */
    private CacheProperties cache = new CacheProperties();

    /**
     * Backward-compatible alias for {@code cache.schema.enabled}.
     * Prefer the nested property structure.
     */
    @Deprecated
    private boolean cacheSchemas = false;

    @Getter
    @Setter
    public static class CacheProperties {
        /**
         * Schema cache settings.
         */
        private SchemaCacheProperties schema = new SchemaCacheProperties();

        /**
         * Cache for expensive flow facet inference snapshots (per flow source), when the flow metadata
         * source is enabled.
         */
        private FacetsCacheProperties facets = new FacetsCacheProperties();
    }

    @Getter
    @Setter
    public static class SchemaCacheProperties {
        /**
         * Enables schema caching across Calcite contexts.
         */
        private boolean enabled = false;

        /**
         * Optional schema cache TTL (for example: 1m, 30s).
         * If not set, cache does not expire automatically.
         */
        private Duration ttl;
    }

    @Getter
    @Setter
    public static class FacetsCacheProperties {
        /**
         * When {@code false}, facet inference always runs on demand (no Caffeine / no-op cache).
         */
        private boolean enabled = true;

        /**
         * Optional facet inference cache TTL (for example: {@code 5m}, {@code PT5M}).
         * If not set, cache does not expire automatically (same style as {@link SchemaCacheProperties#getTtl()}).
         */
        private Duration ttl;
    }

}
