package io.qpointz.mill.sql.dialect;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SQL dialect specification deserialization.
 */
class SqlDialectSpecTest {

    private static final List<String> DIALECT_RESOURCES = Arrays.asList(
            "sql/dialects/calcite/calcite.yml",
            "sql/dialects/databricks/databricks.yml",
            "sql/dialects/db2/db2.yml",
            "sql/dialects/duckdb/duckdb.yml",
            "sql/dialects/h2/h2.yml",
            "sql/dialects/mssql/mssql.yml",
            "sql/dialects/mysql/mysql.yml",
            "sql/dialects/oracle/oracle.yml",
            "sql/dialects/postgres/postgres.yml",
            "sql/dialects/trino/trino.yml"
    );

    @Test
    void shouldDeserializeAllDialectFiles() {
        for (String resource : DIALECT_RESOURCES) {
            assertDoesNotThrow(() -> {
                SqlDialectSpec spec = SqlDialectSpec.fromResource(resource);
                assertNotNull(spec, "Dialect spec should not be null for " + resource);
                assertNotNull(spec.id(), "ID should not be null for " + resource);
                assertNotNull(spec.name(), "Name should not be null for " + resource);
                assertNotNull(spec.identifiers(), "Identifiers should not be null for " + resource);
                assertNotNull(spec.literals(), "Literals should not be null for " + resource);
                assertNotNull(spec.joins(), "Joins should not be null for " + resource);
                assertNotNull(spec.ordering(), "Ordering should not be null for " + resource);
                assertNotNull(spec.paging(), "Paging should not be null for " + resource);
                assertNotNull(spec.operators(), "Operators should not be null for " + resource);
                assertNotNull(spec.functions(), "Functions should not be null for " + resource);
            }, "Should deserialize " + resource + " without exceptions");
        }
    }

    @Test
    void shouldDeserializePostgresDialect() {
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/postgres/postgres.yml");
        
        assertEquals("POSTGRES", spec.id());
        assertEquals("PostgreSQL", spec.name());
        assertEquals(IdentifierCase.AS_IS, spec.identifiers().case_());
        assertEquals("\"", spec.identifiers().quote().start());
        assertEquals("\"", spec.identifiers().quote().end());
        
        // Check literals
        assertEquals("'", spec.literals().strings().quote());
        assertEquals("||", spec.literals().strings().concat());
        assertEquals(StringEscape.STANDARD, spec.literals().strings().escape());
        
        // Check operators
        assertTrue(spec.operators().containsKey("equality"));
        assertTrue(spec.operators().containsKey("inequality"));
        assertTrue(spec.operators().containsKey("comparison"));
        
        // Check functions
        assertTrue(spec.functions().containsKey("strings"));
        assertTrue(spec.functions().containsKey("aggregates"));
        assertTrue(spec.functions().containsKey("dates_times"));
    }

    @Test
    void shouldDeserializeMysqlDialect() {
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/mysql/mysql.yml");
        
        assertEquals("MYSQL", spec.id());
        assertEquals("MySQL", spec.name());
        assertEquals(IdentifierCase.LOWER, spec.identifiers().case_());
        assertEquals("`", spec.identifiers().quote().start());
        
        // Check that CONCAT is used instead of ||
        assertEquals("CONCAT", spec.literals().strings().concat());
        
        // Check that full-join is disabled
        assertFalse(spec.joins().fullJoin().enabled().orElse(true));
        
        // Check ordering notes (which is a string in mysql.yml)
        assertTrue(spec.ordering().notes().isPresent());
    }

    @Test
    void shouldDeserializeCalciteDialect() {
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/calcite/calcite.yml");
        
        assertEquals("CALCITE", spec.id());
        assertEquals("Apache Calcite", spec.name());
        assertEquals(IdentifierCase.UPPER, spec.identifiers().case_());
    }

    @Test
    void shouldDeserializeMssqlDialect() {
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/mssql/mssql.yml");
        
        assertEquals("MSSQL", spec.id());
        assertEquals("Microsoft SQL Server", spec.name());
        
        // Check square brackets for identifiers
        assertEquals("[", spec.identifiers().quote().start());
        assertEquals("]", spec.identifiers().quote().end());
        
        // Check that string concatenation uses +
        assertEquals("+", spec.literals().strings().concat());
        
        // Check TOP clause
        assertTrue(spec.paging().top().isPresent());
        assertEquals("TOP {n}", spec.paging().top().get());
    }

    @Test
    void shouldDeserializeH2Dialect() {
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/h2/h2.yml");
        
        assertEquals("H2", spec.id());
        assertEquals("H2 Database", spec.name());
        
        // Check that notes are present
        assertTrue(spec.notes().isPresent());
        assertFalse(spec.notes().get().isEmpty());
    }

    @Test
    void shouldHandleEmptyOperatorLists() {
        // MSSQL has empty null-safe and regex operator lists
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/mssql/mssql.yml");
        
        assertTrue(spec.operators().containsKey("null-safe"));
        assertTrue(spec.operators().containsKey("regex"));
        
        // Empty lists should deserialize as empty lists, not null
        List<OperatorEntry> nullSafe = spec.operators().get("null-safe");
        List<OperatorEntry> regex = spec.operators().get("regex");
        assertNotNull(nullSafe);
        assertNotNull(regex);
    }

    @Test
    void shouldHandleFunctionArguments() {
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/postgres/postgres.yml");
        
        // Find SUBSTRING function
        List<FunctionEntry> strings = spec.functions().get("strings");
        assertNotNull(strings);
        
        FunctionEntry substring = strings.stream()
                .filter(f -> f.name().equals("SUBSTRING"))
                .findFirst()
                .orElseThrow();
        
        assertTrue(substring.args().isPresent());
        assertEquals(3, substring.args().get().size());
        
        // Check first argument (text)
        FunctionArg textArg = substring.args().get().get(0);
        assertEquals("text", textArg.name());
        assertEquals("STRING", textArg.type());
        assertTrue(textArg.required());
    }

    @Test
    void shouldHandleSynonyms() {
        SqlDialectSpec spec = SqlDialectSpec.fromResource("sql/dialects/postgres/postgres.yml");
        
        List<FunctionEntry> strings = spec.functions().get("strings");
        FunctionEntry substring = strings.stream()
                .filter(f -> f.name().equals("SUBSTRING"))
                .findFirst()
                .orElseThrow();
        
        assertTrue(substring.synonyms().isPresent());
        assertTrue(substring.synonyms().get().contains("SUBSTR"));
    }
}
