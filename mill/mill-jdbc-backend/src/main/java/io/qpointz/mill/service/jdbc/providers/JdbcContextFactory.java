package io.qpointz.mill.service.jdbc.providers;

import java.sql.Connection;

public interface JdbcContextFactory {
    JdbcContext createContext();
}
