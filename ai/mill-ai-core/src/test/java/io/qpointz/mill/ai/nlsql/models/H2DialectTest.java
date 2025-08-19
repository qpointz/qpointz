package io.qpointz.mill.ai.nlsql.models;

public class H2DialectTest extends SpecSqlDialectTestBase {
    @Override
    protected SqlDialect dialect() {
        return SqlDialects.H2;
    }
}

