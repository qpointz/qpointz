package io.qpointz.rapids.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RapidsDriverTest {

    @Test
    void  createDriver() throws SQLException {
        var driver = DriverManager.getDriver("jdbc:rapids:grpc://locahost:18022?kkk=lll&kkkk=qqq");
        assertNotNull(driver);
    }

    @Test
    void  createConnection() throws SQLException {
        var connection = DriverManager.getConnection("jdbc:rapids:grpc://locahost:18022?kkk=lll&kkkk=qqq");
        assertNotNull(connection);
    }


}