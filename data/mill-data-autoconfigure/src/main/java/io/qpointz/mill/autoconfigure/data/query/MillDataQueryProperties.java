package io.qpointz.mill.autoconfigure.data.query;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Bindings for {@code mill.data.query.*} — core session engine tuning for {@code mill-data-query}.
 *
 * <p>Registered by {@link QueryResultEngineAutoConfiguration}; lives in {@code mill-data-autoconfigure}
 * so applications can use {@link io.qpointz.mill.data.query.engine.QueryResultExecutionService} in-process
 * without depending on {@code mill-data-query-service} (REST).
 */
@ConfigurationProperties(prefix = "mill.data.query")
public class MillDataQueryProperties {

    /**
     * When {@code false}, {@link QueryResultEngineAutoConfiguration} does not register engine beans
     * ({@code ResultMarshallerRegistry}, {@code QueryResultExecutionService}).
     */
    private boolean enabled = true;

    private int maxMaterializedRows = 100_000;

    private int defaultFetchSize = 1024;

    private int maxPageSize = 10_000;

    private Duration sessionExpireAfterAccess = Duration.ofMinutes(30);

    /**
     * @return whether the query-result engine beans are registered
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled whether the query-result engine beans are registered
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return maximum rows materialized per session before failing the query
     */
    public int getMaxMaterializedRows() {
        return maxMaterializedRows;
    }

    /**
     * @param maxMaterializedRows maximum rows materialized per session
     */
    public void setMaxMaterializedRows(int maxMaterializedRows) {
        this.maxMaterializedRows = maxMaterializedRows;
    }

    /**
     * @return dispatcher {@code QueryExecutionConfig.fetchSize} default
     */
    public int getDefaultFetchSize() {
        return defaultFetchSize;
    }

    /**
     * @param defaultFetchSize dispatcher fetch size
     */
    public void setDefaultFetchSize(int defaultFetchSize) {
        this.defaultFetchSize = defaultFetchSize;
    }

    /**
     * @return upper bound for presentation {@code pageSize}
     */
    public int getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * @param maxPageSize upper bound for presentation {@code pageSize}
     */
    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    /**
     * @return idle eviction duration for the session cache
     */
    public Duration getSessionExpireAfterAccess() {
        return sessionExpireAfterAccess;
    }

    /**
     * @param sessionExpireAfterAccess idle eviction duration
     */
    public void setSessionExpireAfterAccess(Duration sessionExpireAfterAccess) {
        this.sessionExpireAfterAccess = sessionExpireAfterAccess;
    }
}
