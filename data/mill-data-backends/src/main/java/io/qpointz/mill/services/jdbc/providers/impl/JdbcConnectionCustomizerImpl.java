package io.qpointz.mill.services.jdbc.providers.impl;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.services.jdbc.providers.JdbcConnectionProvider;
import lombok.val;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JdbcConnectionCustomizerImpl implements JdbcConnectionProvider {

    @Override
    public Map<String, Object> customizeSchemaOperand(String targetName, Optional<String> catalog, Optional<String> schema, Map<String, Object> operand) {
        val cloned = new HashMap<String, Object>();
        cloned.putAll(operand);
        return cloned;
    }

    @Override
    public Connection createConnection(String driver, String url, String user, String password) {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url,
                    user,
                    password);
        } catch (SQLException | ClassNotFoundException e) {
            throw new MillRuntimeException(e);
        }
    }
}
