package io.qpointz.mill.ai.nlsql.models;

public class TrinoDialectTest extends SpecSqlDialectTestBase {
    @Override
    protected SqlDialect dialect() {
        return SqlDialects.TRINO;
    }
}

