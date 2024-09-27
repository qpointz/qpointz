package io.qpointz.mill.services.jdbc.providers;

import java.sql.Connection;

public interface JdbcContext {

    Connection getConnection();

}
