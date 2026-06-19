package io.qpointz.mill.persistence.flyway;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link V9__EnsurePgvectorExtension} error classification helpers.
 */
class PgvectorExtensionMigrationTest {

    @Test
    void shouldTreatH2ProductNameAsNonPostgres() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("H2");

        assertThat(V9__EnsurePgvectorExtension.isH2OrNonPostgres(connection)).isTrue();
    }

    @Test
    void shouldTreatPostgreSqlProductNameAsPostgres() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");

        assertThat(V9__EnsurePgvectorExtension.isH2OrNonPostgres(connection)).isFalse();
    }

    @Test
    void shouldClassifyExtensionUnavailableErrorsAsSoftSkip() {
        SQLException unavailable = new SQLException("extension \"vector\" is not available");

        assertThat(V9__EnsurePgvectorExtension.isPgvectorExtensionUnavailable(unavailable)).isTrue();
    }

    @Test
    void shouldNotClassifyPermissionDeniedAsExtensionUnavailable() {
        SQLException permissionDenied = new SQLException("permission denied to create extension \"vector\"");

        assertThat(V9__EnsurePgvectorExtension.isPgvectorExtensionUnavailable(permissionDenied)).isFalse();
    }

    @Test
    void shouldClassifyH2SyntaxErrorsAsNonPostgresSqlError() {
        SQLException syntax = new SQLException("Syntax error in SQL statement");

        assertThat(V9__EnsurePgvectorExtension.isNonPostgresSqlError(syntax)).isTrue();
    }
}
