package io.qpointz.mill.data.backend.flow;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FlowContextFactoryTest {

    private static final Path SOURCE_1 = Path.of("./config/test/flow-source.yaml");
    private static final Path SOURCE_2 = Path.of("./config/test/flow-source-2.yaml");
    private static final Path SKYMILL = Path.of("./config/test/flow-skymill.yaml");

    private FlowContextFactory createFactory(SourceDefinitionRepository repo) {
        return new FlowContextFactory(repo, new Properties());
    }

    @Test
    void shouldCreateContextWithSingleSource() throws Exception {
        val repo = new SingleFileSourceRepository(SOURCE_1);
        val factory = createFactory(repo);

        try (val ctx = factory.createContext()) {
            assertNotNull(ctx);
            assertNotNull(ctx.getCalciteConnection());
            val schemaNames = ctx.getRootSchema().getSubSchemaNames();
            assertTrue(schemaNames.contains("flowtest"));
        }
    }

    @Test
    void shouldCreateContextWithMultipleSources() throws Exception {
        val repo = new MultiFileSourceRepository(List.of(SOURCE_1, SOURCE_2));
        val factory = createFactory(repo);

        try (val ctx = factory.createContext()) {
            assertNotNull(ctx);
            val schemaNames = ctx.getRootSchema().getSubSchemaNames();
            assertTrue(schemaNames.contains("flowtest"));
            assertTrue(schemaNames.contains("flowtest2"));
        }
    }

    @Test
    void shouldExposeTablesInSchema() throws Exception {
        val repo = new SingleFileSourceRepository(SOURCE_1);
        val factory = createFactory(repo);

        try (val ctx = factory.createContext()) {
            val schema = ctx.getRootSchema().getSubSchema("flowtest");
            assertNotNull(schema);
            assertTrue(schema.getTableNames().contains("products"));
        }
    }

    @Test
    void shouldCreateContextWithEmptyRepository() throws Exception {
        val repo = new MultiFileSourceRepository(List.of());
        val factory = createFactory(repo);

        try (val ctx = factory.createContext()) {
            assertNotNull(ctx);
            assertFalse(ctx.getRootSchema().getSubSchemaNames().contains("flowtest"));
        }
    }

    @Test
    void shouldExposeRepository() {
        val repo = new SingleFileSourceRepository(SOURCE_1);
        val factory = createFactory(repo);
        assertSame(repo, factory.getRepository());
    }

    @Test
    void shouldExposeConnectionProperties() {
        val props = new Properties();
        props.setProperty("caseSensitive", "false");
        val repo = new SingleFileSourceRepository(SOURCE_1);
        val factory = new FlowContextFactory(repo, props);
        assertEquals("false", factory.getConnectionProperties().getProperty("caseSensitive"));
    }

    @Test
    void shouldExposeSkymillTables() throws Exception {
        val repo = new SingleFileSourceRepository(SKYMILL);
        val factory = createFactory(repo);

        try (val ctx = factory.createContext()) {
            val schema = ctx.getRootSchema().getSubSchema("skymill");
            assertNotNull(schema);
            val tables = schema.getTableNames();
            assertTrue(tables.contains("cities"));
            assertTrue(tables.contains("aircraft"));
            assertTrue(tables.contains("bookings"));
            assertTrue(tables.contains("passenger"));
            assertTrue(tables.contains("segments"));
        }
    }
}
