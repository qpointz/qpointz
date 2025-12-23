package io.qpointz.mill.sql.dialect;

import lombok.Getter;

import java.util.Map;

/**
 * Provides static instances of SQL dialect specifications loaded from YAML resources.
 * 
 * Each dialect specification is loaded from sql/dialects/{dialect}/{dialect}.yml
 * and provides structured information about the SQL dialect's capabilities.
 */
public class SqlDialectSpecs {

    /** Apache Calcite SQL dialect specification. */
    public static final SqlDialectSpec CALCITE = SqlDialectSpec
            .fromResource("sql/dialects/calcite/calcite.yml");

    /** Databricks SQL (Spark SQL) dialect specification. */
    public static final SqlDialectSpec DATABRICKS = SqlDialectSpec
            .fromResource("sql/dialects/databricks/databricks.yml");

    /** H2 Database SQL dialect specification. */
    public static final SqlDialectSpec H2 = SqlDialectSpec
            .fromResource("sql/dialects/h2/h2.yml");

    /** PostgreSQL SQL dialect specification. */
    public static final SqlDialectSpec POSTGRES = SqlDialectSpec
            .fromResource("sql/dialects/postgres/postgres.yml");

    /** Trino SQL dialect specification. */
    public static final SqlDialectSpec TRINO = SqlDialectSpec
            .fromResource("sql/dialects/trino/trino.yml");

    /** IBM DB2 SQL dialect specification. */
    public static final SqlDialectSpec DB2 = SqlDialectSpec
            .fromResource("sql/dialects/db2/db2.yml");

    /** DuckDB SQL dialect specification. */
    public static final SqlDialectSpec DUCKDB = SqlDialectSpec
            .fromResource("sql/dialects/duckdb/duckdb.yml");

    /** Microsoft SQL Server SQL dialect specification. */
    public static final SqlDialectSpec MSSQL = SqlDialectSpec
            .fromResource("sql/dialects/mssql/mssql.yml");

    /** MySQL SQL dialect specification. */
    public static final SqlDialectSpec MYSQL = SqlDialectSpec
            .fromResource("sql/dialects/mysql/mysql.yml");

    /** Oracle Database SQL dialect specification. */
    public static final SqlDialectSpec ORACLE = SqlDialectSpec
            .fromResource("sql/dialects/oracle/oracle.yml");

    /**
     * Map of all SQL dialect specifications keyed by dialect ID.
     * The map keys are the dialect IDs (e.g., "POSTGRES", "MYSQL", "CALCITE").
     */
    @Getter
    private static final Map<String, SqlDialectSpec> specs = Map.of(
            CALCITE.id(), CALCITE,
            DATABRICKS.id(), DATABRICKS,
            H2.id(), H2,
            POSTGRES.id(), POSTGRES,
            TRINO.id(), TRINO,
            DB2.id(), DB2,
            DUCKDB.id(), DUCKDB,
            MSSQL.id(), MSSQL,
            MYSQL.id(), MYSQL,
            ORACLE.id(), ORACLE
    );

    private SqlDialectSpecs() {
        // Avoid instantiation
    }

    /**
     * Look up a SQL dialect specification by its ID.
     * 
     * @param dialectId The dialect ID (e.g., "POSTGRES", "MYSQL", "CALCITE")
     * @return The SqlDialectSpec for the given ID, or null if not found
     */
    public static SqlDialectSpec byId(String dialectId) {
        return dialectId == null ? null : specs.get(dialectId);
    }
}
