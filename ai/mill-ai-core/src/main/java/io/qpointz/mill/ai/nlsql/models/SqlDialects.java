package io.qpointz.mill.ai.nlsql.models;

import java.util.Map;

public class SqlDialects {

    private SqlDialects() {
        //avoid instantiation
    }

    private static Map<String, SqlDialect> dialects = Map.of();

    public static SqlDialect byName(String dialectName) {
        return dialectName==null
                ? new DefaultSqlDialect()
                : dialects.get(dialectName);
    }
}