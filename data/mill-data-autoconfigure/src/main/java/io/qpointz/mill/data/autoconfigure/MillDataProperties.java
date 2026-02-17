package io.qpointz.mill.data.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties bound to the {@code mill.data.*} prefix.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "mill.data")
public class MillDataProperties {

    /**
     * SQL dialect identifier used to resolve a
     * {@link io.qpointz.mill.sql.dialect.SqlDialectSpec} via
     * {@link io.qpointz.mill.sql.dialect.SqlDialectSpecs#byId(String)}.
     * Supported values: CALCITE, POSTGRES, MYSQL, MSSQL, ORACLE, H2, TRINO, DATABRICKS, DB2, DUCKDB.
     */
    private String sqlDialect = "calcite";

    /**
     * Data backend identifier that determines which execution backend to use.
     */
    private String backend = "calcite";

}
