package io.qpointz.mill;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes the shared skymill SQL query set via the JDBC driver.
 *
 * The query list is shared with the gRPC server integration tests through the repository-level
 * `test/it-querycases/skymill-sql.json` file to keep expectations aligned across transports.
 */
class SkymillSharedQuerySetJdbcIT {

    private static final String QUERY_CASES_RELATIVE = "test/it-querycases/skymill-sql.json";

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void shouldExecuteSharedSkymillQuerySetOverJdbc(TestITProfile profile) throws Exception {
        val queryCases = resolveQueryCasesPath();
        assertTrue(Files.exists(queryCases), "Missing query-case file at " + queryCases.toAbsolutePath());

        val mapper = new ObjectMapper();
        val cases = List.of(mapper.readValue(Files.readAllBytes(queryCases), QueryCase[].class));
        assertFalse(cases.isEmpty(), "Expected at least one query case");

        try (val conn = DriverManager.getConnection(profile.jdbcUrl(), profile.connectionProperties())) {
            for (QueryCase c : cases) {
                val sql = c.sql.replace("{schema}", profile.schemaName());
                try (val stmt = conn.createStatement()) {
                    val hasResult = stmt.execute(sql);
                    assertTrue(hasResult, "[" + c.id + "] expected a ResultSet");

                    try (val rs = stmt.getResultSet()) {
                        assertNotNull(rs, "[" + c.id + "] ResultSet must not be null");
                        assertRequiredColumns(rs.getMetaData(), c, c.id);

                        long rows = 0;
                        Integer equalsIdx = null;
                        if (c.allRowsColumnEquals != null) {
                            equalsIdx = columnIndex(rs.getMetaData(), c.allRowsColumnEquals.column);
                            assertNotNull(equalsIdx, "[" + c.id + "] column not found: " + c.allRowsColumnEquals.column);
                        }

                        while (rs.next()) {
                            rows++;
                            if (equalsIdx != null) {
                                val actual = rs.getString(equalsIdx);
                                assertEquals(c.allRowsColumnEquals.value, actual, "[" + c.id + "] unexpected column value");
                            }
                        }

                        assertTrue(rows >= c.minRows, "[" + c.id + "] expected minRows=" + c.minRows + " got=" + rows);
                        if (c.maxRows != null) {
                            assertTrue(rows <= c.maxRows, "[" + c.id + "] expected maxRows=" + c.maxRows + " got=" + rows);
                        }
                    }
                }
            }
        }
    }

    private static Path resolveQueryCasesPath() {
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 8; i++) {
            Path candidate = dir.resolve(QUERY_CASES_RELATIVE);
            if (Files.exists(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
            if (dir == null) break;
        }
        return Paths.get(QUERY_CASES_RELATIVE);
    }

    private static void assertRequiredColumns(ResultSetMetaData meta, QueryCase c, String caseId) throws Exception {
        val required = c.requiredColumns.stream().map(String::toLowerCase).collect(Collectors.toSet());
        val actual = new HashSet<String>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            val label = meta.getColumnLabel(i);
            if (label != null) actual.add(label.toLowerCase());
        }
        assertTrue(actual.containsAll(required), "[" + caseId + "] missing required columns: " + required + " actual=" + actual);
    }

    private static Integer columnIndex(ResultSetMetaData meta, String name) throws Exception {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            val label = meta.getColumnLabel(i);
            if (label != null && label.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QueryCase {
        public String id;
        public String description;
        public String sql;
        public long minRows;
        public Long maxRows;
        public List<String> requiredColumns = List.of();
        public ColumnEquals allRowsColumnEquals;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ColumnEquals {
        public String column;
        public String value;
    }
}

