package io.qpointz.mill.sql.dialect;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SqlDialectSpecs.
 */
class SqlDialectSpecsTest {

    @Test
    void shouldLoadAllDialectSpecs() {
        assertNotNull(SqlDialectSpecs.CALCITE);
        assertNotNull(SqlDialectSpecs.DATABRICKS);
        assertNotNull(SqlDialectSpecs.H2);
        assertNotNull(SqlDialectSpecs.POSTGRES);
        assertNotNull(SqlDialectSpecs.TRINO);
        assertNotNull(SqlDialectSpecs.DB2);
        assertNotNull(SqlDialectSpecs.DUCKDB);
        assertNotNull(SqlDialectSpecs.MSSQL);
        assertNotNull(SqlDialectSpecs.MYSQL);
        assertNotNull(SqlDialectSpecs.ORACLE);
    }

    @Test
    void shouldHaveCorrectIds() {
        assertEquals("CALCITE", SqlDialectSpecs.CALCITE.id());
        assertEquals("DATABRICKS", SqlDialectSpecs.DATABRICKS.id());
        assertEquals("H2", SqlDialectSpecs.H2.id());
        assertEquals("POSTGRES", SqlDialectSpecs.POSTGRES.id());
        assertEquals("TRINO", SqlDialectSpecs.TRINO.id());
        assertEquals("DB2", SqlDialectSpecs.DB2.id());
        assertEquals("DUCKDB", SqlDialectSpecs.DUCKDB.id());
        assertEquals("MSSQL", SqlDialectSpecs.MSSQL.id());
        assertEquals("MYSQL", SqlDialectSpecs.MYSQL.id());
        assertEquals("ORACLE", SqlDialectSpecs.ORACLE.id());
    }

    @Test
    void shouldLookupById() {
        assertEquals(SqlDialectSpecs.POSTGRES, SqlDialectSpecs.byId("POSTGRES"));
        assertEquals(SqlDialectSpecs.MYSQL, SqlDialectSpecs.byId("MYSQL"));
        assertEquals(SqlDialectSpecs.CALCITE, SqlDialectSpecs.byId("CALCITE"));
        assertEquals(SqlDialectSpecs.H2, SqlDialectSpecs.byId("H2"));
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(SqlDialectSpecs.byId("UNKNOWN"));
        assertNull(SqlDialectSpecs.byId(null));
    }

    @Test
    void shouldHaveAllSpecsInMap() {
        Map<String, SqlDialectSpec> specs = SqlDialectSpecs.getSpecs();
        
        assertEquals(10, specs.size());
        assertTrue(specs.containsKey("CALCITE"));
        assertTrue(specs.containsKey("DATABRICKS"));
        assertTrue(specs.containsKey("H2"));
        assertTrue(specs.containsKey("POSTGRES"));
        assertTrue(specs.containsKey("TRINO"));
        assertTrue(specs.containsKey("DB2"));
        assertTrue(specs.containsKey("DUCKDB"));
        assertTrue(specs.containsKey("MSSQL"));
        assertTrue(specs.containsKey("MYSQL"));
        assertTrue(specs.containsKey("ORACLE"));
    }

    @Test
    void shouldHaveCorrectNames() {
        assertEquals("PostgreSQL", SqlDialectSpecs.POSTGRES.name());
        assertEquals("MySQL", SqlDialectSpecs.MYSQL.name());
        assertEquals("Apache Calcite", SqlDialectSpecs.CALCITE.name());
        assertEquals("H2 Database", SqlDialectSpecs.H2.name());
        assertEquals("Microsoft SQL Server", SqlDialectSpecs.MSSQL.name());
    }
}
