package io.qpointz.mill.ai.nlsql.models;

public class CalciteDialectTest extends SpecSqlDialectTestBase {
    @Override
    protected SqlDialect dialect() {
        return SqlDialects.CALCITE;
    }
}
