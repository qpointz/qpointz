package io.qpointz.mill.ai.nlsql.models;

import lombok.Getter;

import java.util.Map;

public class SqlDialects {

    public static final SqlDialect DEFAULT = new DefaultSqlDialect();

    public static final SqlDialect CALCITE = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/calcite/calcite.yml");

    public static final SqlDialect DATABRICKS = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/databricks/databricks.yml");

    public static final SqlDialect H2 = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/h2/h2.yml");

    public static final SqlDialect POSTGRES = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/postgres/postgres.yml");

    public static final SqlDialect TRINO = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/trino/trino.yml");

    public static final SqlDialect DB2 = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/db2/db2.yml");

    public static final SqlDialect DUCKDB = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/duckdb/duckdb.yml");

    public static final SqlDialect MSSQL = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/mssql/mssql.yml");

    public static final SqlDialect MYSQL = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/mysql/mysql.yml");

    public static final SqlDialect ORACLE = SpecSqlDialect
            .fromResource("templates/nlsql/dialects/oracle/oracle.yml");

    @Getter
    private static final Map<String, SqlDialect> dialects = Map.of(
            CALCITE.getId(), CALCITE,
            DATABRICKS.getId(), DATABRICKS,
            H2.getId(), H2,
            POSTGRES.getId(), POSTGRES,
            TRINO.getId(), TRINO,
            DB2.getId(), DB2,
            DUCKDB.getId(), DUCKDB,
            MSSQL.getId(), MSSQL,
            MYSQL.getId(), MYSQL,
            ORACLE.getId(), ORACLE
    );


    private SqlDialects() {
        //avoid instantiation
    }

    public static SqlDialect byName(String dialectName) {
        return dialectName==null
                ? DEFAULT
                : dialects.get(dialectName);
    }
}