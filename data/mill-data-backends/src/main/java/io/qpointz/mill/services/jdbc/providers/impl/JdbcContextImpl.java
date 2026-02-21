package io.qpointz.mill.services.jdbc.providers.impl;

import io.qpointz.mill.services.jdbc.JdbcCalciteConfiguration;
import io.qpointz.mill.services.jdbc.providers.JdbcConnectionProvider;
import io.qpointz.mill.services.jdbc.providers.JdbcContext;
import lombok.AllArgsConstructor;

import java.sql.Connection;

@AllArgsConstructor
public class JdbcContextImpl implements JdbcContext {

    private final JdbcCalciteConfiguration config;

    private final JdbcConnectionProvider jdbcConnectionProvider;

    @Override
    public Connection getConnection() {
        return jdbcConnectionProvider.createConnection(
                this.config.getDriver(),
                this.config.getUrl(),
                this.config.getUser().orElse(""),
                this.config.getPassword().orElse(""));
    }
}
