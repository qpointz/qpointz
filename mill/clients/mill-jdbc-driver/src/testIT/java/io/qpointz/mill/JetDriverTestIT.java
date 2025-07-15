package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JetDriverTestIT {

    private Connection connection(TestITProfile profile, Map<?, ?> overrides) throws SQLException {
        String protocol = switch (profile.protocol()) {
            case GRPC -> "grpc";
            case HTTP -> profile.tls() ? "https" : "http";
        };

        String path = switch (profile.protocol()) {
            case GRPC -> "";
            case HTTP -> "/services/jet/";
        };

        val url =  String.format("jdbc:mill:%s://%s:%s%s",
          protocol,
          profile.host(),
          profile.port(),
          path);
        log.info("Connection string {}", url);

        val info = new Properties();
        info.putAll(overrides);

        val connection = DriverManager.getConnection(url, info);
        assertNotNull(connection);
        return connection;
    }

    private Connection connection(TestITProfile profile) throws SQLException {
        return connection(profile, Map.<String,Object>of());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void createConnection(TestITProfile profile) {
        assertDoesNotThrow(() -> connection(profile));
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void statmentMetaDataTest(TestITProfile profile) throws ClassNotFoundException, SQLException {
        val stmt = connection(profile)
                .createStatement();
        stmt.setFetchSize(20);
        val rs = stmt.executeQuery("select `CLIENT_ID`, `FIRST_NAME`, `LAST_NAME` from `MONETA`.`CLIENTS`");
        var rowId = 0;
        val md = rs.getMetaData();
        assertEquals(3, md.getColumnCount());
        while (rs.next()) {
            rowId++;
        }
        assertTrue(rowId>1);
    }

}
