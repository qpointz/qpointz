package io.qpointz.mill.data.backend.jdbc.providers.impl;

import io.qpointz.mill.data.backend.jdbc.JdbcCalciteConfiguration;
import io.qpointz.mill.data.backend.jdbc.providers.JdbcConnectionProvider;
import io.qpointz.mill.data.backend.jdbc.providers.JdbcContext;
import io.qpointz.mill.data.backend.jdbc.providers.JdbcContextFactory;
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
