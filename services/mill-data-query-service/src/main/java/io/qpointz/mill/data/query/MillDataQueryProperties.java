package io.qpointz.mill.data.query;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Bindings for {@code mill.data.query.*} — core session engine tuning for {@code mill-data-query}.
 */
@ConditionalOnService(value = "query", group = "data")
@ConfigurationProperties(prefix = "mill.data.query")
public class MillDataQueryProperties {

    private int maxMaterializedRows = 100_000;

    private int defaultFetchSize = 1024;

    private int maxPageSize = 10_000;

    private Duration sessionExpireAfterAccess = Duration.ofMinutes(30);

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
