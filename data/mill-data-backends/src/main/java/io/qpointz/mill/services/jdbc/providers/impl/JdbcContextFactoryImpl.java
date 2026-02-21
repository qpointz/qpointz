package io.qpointz.mill.services.jdbc.providers.impl;

import io.qpointz.mill.services.jdbc.JdbcCalciteConfiguration;
import io.qpointz.mill.services.jdbc.providers.JdbcConnectionProvider;
import io.qpointz.mill.services.jdbc.providers.JdbcContext;
import io.qpointz.mill.services.jdbc.providers.JdbcContextFactory;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JdbcContextFactoryImpl implements JdbcContextFactory {

    private final JdbcCalciteConfiguration config;

    private final JdbcConnectionProvider jdbcConnectionProvider;

    @Override
    public JdbcContext createContext() {
        return new JdbcContextImpl(config, jdbcConnectionProvider);
    }
}
