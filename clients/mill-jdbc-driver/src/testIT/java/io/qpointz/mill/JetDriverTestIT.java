package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JetDriverTestIT {

    private Connection connection(TestITProfile profile) throws SQLException {
        val url = profile.jdbcUrl();
        log.info("Integration parameters: {}", profile.debugSummary());
        log.info("JDBC URL: {}", url);
        log.info("Connection properties: {}", profile.maskedConnectionProperties());

        val connection = DriverManager.getConnection(url, profile.connectionProperties());
        assertNotNull(connection);
        return connection;
    }

    private static List<String> columnNames(ResultSet rs) throws SQLException {
        val md = rs.getMetaData();
        val names = new ArrayList<String>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            names.add(md.getColumnLabel(i).toLowerCase(Locale.ROOT));
        }
        return names;
    }

    private static int countRows(ResultSet rs) throws SQLException {
        int count = 0;
        while (rs.next()) {
            count++;
        }
        return count;
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies the JDBC URL can establish a live connection.
    void createConnection(TestITProfile profile) {
        assertDoesNotThrow(() -> connection(profile));
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies DatabaseMetaData#getSchemas includes the configured test schema.
    void metadataSchemasContainConfiguredSchema(TestITProfile profile) throws SQLException {
        try (val con = connection(profile);
             val rs = con.getMetaData().getSchemas()) {
            DatabaseMetaData meta = con.getMetaData();
            assertNotNull(meta);
            Set<String> schemas = new HashSet<>();
            while (rs.next()) {
                schemas.add(rs.getString("TABLE_SCHEM").toLowerCase(Locale.ROOT));
            }
            assertTrue(schemas.contains(profile.schemaName().toLowerCase(Locale.ROOT)));
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies statement metadata is available and rows can be consumed.
    void statmentMetaDataTest(TestITProfile profile) throws ClassNotFoundException, SQLException {
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            stmt.setFetchSize(20);
            val rs = stmt.executeQuery("select * from `" + profile.schemaName() + "`.`cities` limit 20");
            val md = rs.getMetaData();
            assertTrue(md.getColumnCount() > 0);
            assertTrue(countRows(rs) > 0);
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies projected column labels are preserved for simple SELECT.
    void selectCitiesHasExpectedColumns(TestITProfile profile) throws SQLException {
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            val rs = stmt.executeQuery("select `id`, `city`, `state` from `" + profile.schemaName() + "`.`cities`");
            val cols = columnNames(rs);
            assertTrue(cols.contains("id"));
            assertTrue(cols.contains("city"));
            assertTrue(cols.contains("state"));
            assertTrue(countRows(rs) > 0);
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies WHERE filtering returns only matching rows.
    void whereClauseReturnsFilteredRows(TestITProfile profile) throws SQLException {
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            val rs = stmt.executeQuery(
                    "select `id`, `name` from `" + profile.schemaName() + "`.`aircraft_types` where `name` = 'narrow'"
            );
            int rows = 0;
            while (rs.next()) {
                rows++;
                assertEquals("narrow", rs.getString("name"));
            }
            assertTrue(rows >= 1);
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies empty results still expose result-set metadata.
    void impossibleWhereReturnsEmptyWithMetadata(TestITProfile profile) throws SQLException {
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            val rs = stmt.executeQuery(
                    "select `id`, `city` from `" + profile.schemaName() + "`.`cities` where `id` < -999999"
            );
            val cols = columnNames(rs);
            assertTrue(cols.contains("id"));
            assertTrue(cols.contains("city"));
            assertEquals(0, countRows(rs));
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies JOIN queries return expected aliased columns.
    void joinSegmentsCitiesReturnsOriginCity(TestITProfile profile) throws SQLException {
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            val sql = "select `s`.`id`, `s`.`distance`, `c`.`city` as `origin_city` "
                    + "from `" + profile.schemaName() + "`.`segments` as `s` "
                    + "join `" + profile.schemaName() + "`.`cities` as `c` on `s`.`origin` = `c`.`id`";
            val rs = stmt.executeQuery(sql);
            val cols = columnNames(rs);
            assertTrue(cols.contains("origin_city"));
            assertTrue(countRows(rs) > 0);
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies multi-page reads for larger result sets with fetch size.
    void largeQueryCargoShipmentsPaging(TestITProfile profile) throws SQLException {
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            stmt.setFetchSize(100);
            val rs = stmt.executeQuery(
                    "select `id`, `weight_kg`, `revenue` from `" + profile.schemaName() + "`.`cargo_shipments`"
            );
            assertTrue(countRows(rs) > 100);
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies paging behavior remains stable on another large table.
    void bookingsPaging(TestITProfile profile) throws SQLException {
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            stmt.setFetchSize(50);
            val rs = stmt.executeQuery(
                    "select `id`, `passenger_id`, `seat_number` from `" + profile.schemaName() + "`.`bookings`"
            );
            assertTrue(countRows(rs) > 50);
        }
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies repeated execution of same SQL yields stable row counts.
    void repeatedExecutionHasStableRowCount(TestITProfile profile) throws SQLException {
        String sql = "select `id`, `city` from `" + profile.schemaName() + "`.`cities`";
        try (val con = connection(profile);
             val stmt = con.createStatement()) {
            int firstCount = countRows(stmt.executeQuery(sql));
            int secondCount = countRows(stmt.executeQuery(sql));
            assertTrue(firstCount > 0);
            assertEquals(firstCount, secondCount);
        }
    }

}
