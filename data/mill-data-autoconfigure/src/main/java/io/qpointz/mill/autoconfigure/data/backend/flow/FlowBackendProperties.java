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
     * Paths to source descriptor YAML files.
     * Each descriptor becomes a Calcite schema whose name is the descriptor's {@code name} property.
     */
    private List<String> sources = new ArrayList<>();

    /**
     * Flow inferred metadata facets (Data Model / {@code MetadataSource}) — registration and merge.
     * When {@link MetadataProperties#enabled} is {@code false}, the flow descriptor metadata source
     * bean must not be wired; query and schema features of the flow backend are unaffected.
     */
    private MetadataProperties metadata = new MetadataProperties();

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
    public static class MetadataProperties {
        /**
         * When {@code false}, the flow descriptor {@code MetadataSource} is not registered and no flow
         * facet contributions ({@code originId} {@code flow}) participate in merge or REST. Defaults to
         * {@code true}.
         */
        private boolean enabled = true;
    }

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
