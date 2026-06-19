package io.qpointz.mill.persistence.flyway;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/**
 * Flyway V9 — optionally ensure the PostgreSQL {@code vector} extension (pgvector) exists.
 *
 * <p>On H2 and other non-PostgreSQL databases this migration is a no-op and always succeeds so
 * {@code testIT} Flyway runs are not blocked. On PostgreSQL without pgvector installed, the migration
 * logs a warning and succeeds. Permission errors on PostgreSQL fail Flyway so misconfigured grants
 * are surfaced during deployment.
 */
public class V9__EnsurePgvectorExtension extends BaseJavaMigration {

    private static final Logger log = LoggerFactory.getLogger(V9__EnsurePgvectorExtension.class);

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        if (isH2OrNonPostgres(conn)) {
            log.debug(
                    "Skipping pgvector extension migration on non-PostgreSQL database ({})",
                    safeProductName(conn)
            );
            return;
        }
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS vector");
        } catch (SQLException e) {
            if (isPgvectorExtensionUnavailable(e)) {
                log.warn("Skipping pgvector extension: {}", e.getMessage());
                return;
            }
            if (isH2OrNonPostgres(conn) || isNonPostgresSqlError(e)) {
                log.debug("Skipping pgvector extension after non-PostgreSQL error: {}", e.getMessage());
                return;
            }
            throw e;
        }
    }

    /**
     * Returns {@code true} when the connection targets H2 or any database that is not PostgreSQL.
     *
     * @param conn active Flyway connection
     * @return whether extension SQL must be skipped
     * @throws SQLException when database metadata cannot be read
     */
    static boolean isH2OrNonPostgres(Connection conn) throws SQLException {
        String product = conn.getMetaData().getDatabaseProductName();
        if (product == null) {
            return true;
        }
        String normalized = product.toLowerCase(Locale.ROOT);
        return normalized.contains("h2") || !normalized.contains("postgresql");
    }

    /**
     * Returns {@code true} for SQL errors typical of non-PostgreSQL engines executing
     * {@code CREATE EXTENSION}.
     *
     * @param e SQL error from extension creation
     * @return whether the error should be treated as a benign non-Postgres skip
     */
    static boolean isNonPostgresSqlError(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("H2")
                || message.contains("Syntax error")
                || message.contains("CREATE EXTENSION");
    }

    /**
     * Returns {@code true} when PostgreSQL reports that pgvector is not installed or unavailable.
     *
     * @param e SQL error from {@code CREATE EXTENSION vector}
     * @return whether Flyway should soft-skip extension creation
     */
    static boolean isPgvectorExtensionUnavailable(SQLException e) {
        String message = e.getMessage();
        if (message != null) {
            String lower = message.toLowerCase(Locale.ROOT);
            if (lower.contains("extension \"vector\" is not available")
                    || lower.contains("could not open extension control file")
                    || lower.contains("vector.control")) {
                return true;
            }
        }
        String sqlState = e.getSQLState();
        return "0A000".equals(sqlState);
    }

    private static String safeProductName(Connection conn) {
        try {
            return conn.getMetaData().getDatabaseProductName();
        } catch (SQLException ex) {
            return "unknown";
        }
    }
}
