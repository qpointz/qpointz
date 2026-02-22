package io.qpointz.mill.data.backend.jdbc.providers;

import java.sql.Connection;
import java.util.Map;
import java.util.Optional;

public interface JdbcConnectionProvider {

    Map<String, Object> customizeSchemaOperand(String targetName,
                                               Optional<String> catalog,
                                               Optional<String> schema,
                                               Map<String, Object> operand);

    Connection createConnection(String driver, String url, String user, String password);
}
