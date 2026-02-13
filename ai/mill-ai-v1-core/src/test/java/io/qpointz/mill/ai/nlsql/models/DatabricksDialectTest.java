package io.qpointz.mill.ai.nlsql.models;

public class DatabricksDialectTest extends SpecSqlDialectTestBase {

    @Override
    protected SqlDialect dialect() {
        return SqlDialects.DATABRICKS;
    }

}

