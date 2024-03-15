package io.qpointz.rapids.jdbc;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RapidsDriverTest {

    private static TestGrpcServer ctx;

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        ctx = TestGrpcServer.createServer();
    }

    @AfterAll
    public static void afterAll() {
        ctx.shutdown();
    }

    Connection connection() throws SQLException {
        var url = String.format("jdbc:rapids:grpc://127.0.0.1:%d", ctx.getServer().getPort());
        var connection = DriverManager.getConnection(url);
        return connection;
    }

    @Test
    void connectToTest() throws SQLException {
        final var conn = connection();
        conn.close();
    }

    @Test
    @Disabled
    void execStatement() throws SQLException {
        final var conn = connection();
        final var stmt = conn.prepareStatement("SELECT * FROM `demo`.`table`");
        final var rs = stmt.executeQuery();
        assertTrue(rs.next(), "expect non empty result set");
    }

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