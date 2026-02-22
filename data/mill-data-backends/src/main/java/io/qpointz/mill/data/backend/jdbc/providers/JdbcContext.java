package io.qpointz.mill.data.backend.jdbc.providers;

import java.sql.Connection;

public interface JdbcContext {

    Connection getConnection();

}
