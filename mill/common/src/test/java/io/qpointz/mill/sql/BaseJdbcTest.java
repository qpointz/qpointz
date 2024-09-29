package io.qpointz.mill.sql;

import lombok.val;
import org.h2.jdbc.JdbcConnection;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class BaseJdbcTest {

    protected JdbcConnection createDb(String schemaName, String scriptPath) {
        try {
            Class.forName("org.h2.Driver");
            val url = String.format("jdbc:h2:mem:%s;INIT=CREATE SCHEMA IF NOT EXISTS %s", schemaName, schemaName);
            val conn = DriverManager
                    .getConnection(url,"admin","password")
                    .unwrap(org.h2.jdbc.JdbcConnection.class);

            val fileStream = BaseJdbcTest.class.getResourceAsStream(scriptPath);
            val reader = new InputStreamReader(Objects.requireNonNull(fileStream));
            org.h2.tools.RunScript.execute(conn, reader);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected ResultSet execute (Connection conn, String sql) {
        try {
            val stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
