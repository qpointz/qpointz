package io.qpointz.mill.ai.nlsql.models;

public class PostgresDialectTest extends SpecSqlDialectTestBase {
    @Override
    protected SqlDialect dialect() {
        return SqlDialects.POSTGRES;
    }
}

