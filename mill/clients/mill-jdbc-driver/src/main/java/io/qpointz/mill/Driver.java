package io.qpointz.mill;

import io.qpointz.mill.client.MillClientConfiguration;
import io.qpointz.mill.client.MillUrlParser;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public class Driver implements java.sql.Driver {

    private static final Driver INSTANCE = new Driver();
    private static boolean registered = false;
    private static int majorVersion;
    private static int minorVersion;

    public Driver() {
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!this.acceptsURL(url)) {
            return null;
        }

        val props = MillUrlParser.apply(url, info);
        final var config = MillClientConfiguration.builder()
                .fromProperties(props)
                .build();
        return new MillConnection(config);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return MillUrlParser.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        val all = MillUrlParser.KNOWN_PROPERTIES.stream()
                .map(z-> new DriverPropertyInfo(z.key(), z.description()))
                .toList();
        return all.toArray(new DriverPropertyInfo[0]);
    }

    @Override
    public int getMajorVersion() {
        return Driver.majorVersion;
    }

    @Override
    public int getMinorVersion() {
        return Driver.minorVersion;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return log;
    }

    public static synchronized Driver load() {
        if (!registered) {
            registered = true;
            try {
                DriverManager.registerDriver(INSTANCE);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to register Rapids driver", e);
            }
        }

        try {
            val bytes = io.qpointz.mill.Driver.class.getResourceAsStream("/VERSION").readAllBytes();
            val v = new String(bytes).split("\\.");
            Driver.majorVersion = Integer.parseInt(v[0]);
            Driver.minorVersion = Integer.parseInt(v[1]);


        } catch (Exception ex) {
            log.info(String.format("Cant read version %s", ex.getMessage()));
            Driver.majorVersion = 0;
            Driver.minorVersion = 0;
        }

        return INSTANCE;
    }

    static {
        load();
    }
}
