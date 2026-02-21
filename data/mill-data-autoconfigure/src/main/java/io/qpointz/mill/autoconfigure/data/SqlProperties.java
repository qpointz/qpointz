package io.qpointz.mill.autoconfigure.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import static io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration.MILL_DATA_DEFAULT_DIALECT;
import static io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration.MILL_DATA_SQL_CONFIG_KEY;

/**
 * Configuration properties bound to the {@code mill.data.sql} prefix.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = MILL_DATA_SQL_CONFIG_KEY)
public class SqlProperties {

    /**
     * SQL dialect identifier used to resolve a
     * {@link io.qpointz.mill.sql.dialect.SqlDialectSpec} via
     * {@link io.qpointz.mill.sql.dialect.SqlDialectSpecs#byId(String)}.
     * Supported values: CALCITE, POSTGRES, MYSQL, MSSQL, ORACLE, H2, TRINO, DATABRICKS, DB2, DUCKDB.
     */
    private String dialect = MILL_DATA_DEFAULT_DIALECT.id();

    private Map<String, Object> conventions = new HashMap<>();

}
