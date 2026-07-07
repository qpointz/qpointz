package io.qpointz.mill.ai.autoconfigure.sqlquery;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Row limits and optional timeout for {@code sql-query.describe_sql} / {@code sql-query.execute_sql}.
 *
 * <p>Bound from {@code mill.ai.sql-query.execution.*}.
 */
@ConfigurationProperties(prefix = "mill.ai.sql-query.execution")
public class SqlQueryExecutionProperties {

    /**
     * Default {@code max_rows} when callers omit the tool argument.
     */
    private int maxRowsDefault = 1_000;

    /**
     * Hard cap applied to caller {@code max_rows} and full-mode accumulation.
     */
    private int maxRowsHard = 5_000;

    /**
     * Optional execution timeout (reserved for future enforcement).
     */
    private Duration timeout;

    public int getMaxRowsDefault() {
        return maxRowsDefault;
    }

    public void setMaxRowsDefault(int maxRowsDefault) {
        this.maxRowsDefault = maxRowsDefault;
    }

    public int getMaxRowsHard() {
        return maxRowsHard;
    }

    public void setMaxRowsHard(int maxRowsHard) {
        this.maxRowsHard = maxRowsHard;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
