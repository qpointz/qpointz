package io.qpointz.rapids.jdbc;

import lombok.extern.java.Log;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public class RapidsDriver implements Driver  {
    private static final RapidsDriver INSTANCE = new RapidsDriver();
    private static boolean registered = false;

    public RapidsDriver() {
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!this.acceptsURL(url)) {
            return null;
        }

        final var config = RapidsConnectionConfig.from(url, info);
        return new RapidsConnection(config);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(RapidsConnectionConfig.URL_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return log;
    }

    public static synchronized RapidsDriver load() {
        if (!registered) {
            registered = true;
            try {
                DriverManager.registerDriver(INSTANCE);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to register Rapids driver", e);
            }
        }

        return INSTANCE;
    }

    static {
        load();
    }
}
