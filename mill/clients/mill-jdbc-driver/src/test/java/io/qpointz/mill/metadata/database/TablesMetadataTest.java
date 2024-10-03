package io.qpointz.mill.metadata.database;

import io.qpointz.mill.InProcessTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TablesMetadataTest extends InProcessTest {

    private Stream<TablesMetadata.TableRecord> collect(String catalogPattern, String schemaPattern, String tableNamePattern, String[] typesPattern) {
        val con = this.createConnection();
        return new TablesMetadata(con, catalogPattern, schemaPattern, tableNamePattern, typesPattern)
                .getMetadata().stream();
    }

    @Test
    void trivia() {
        val schemas = collect(null, null, null, null)
                .map(k-> k.schema())
                .collect(Collectors.toSet());
        assertEquals(Set.of("test", "airlines", "metadata"), schemas);
    }

    @Test
    void byCatalogPattern() {
        assertEquals(
                collect(null, null, null, null).count(),
                collect("", null, null, null).count()
        );
    }

    @Test
    void bySchemaPattern() {
        assertEquals(0,collect(null, "", null, null)
                .count());

        assertEquals(1,collect(null, "test", null, null)
                .map(TablesMetadata.TableRecord::schema).distinct()
                .count());

    }

    @Test
    void byTableName() {
        assertEquals(7,collect(null, null, null, null)
                .count());

        assertEquals(0,collect(null, null, "", null)
                .count());

        assertEquals(1,collect(null, "airlines", "cities", null)
                .map(TablesMetadata.TableRecord::schema).distinct()
                .count());

        assertEquals(0,collect(null, "airlines", "notable", null)
                .map(TablesMetadata.TableRecord::schema).distinct()
                .count());

    }

    @Test
    void byTableType() {
        assertEquals(7,collect(null, null, null, new String[]{})
                .count());

        assertEquals(1,collect(null, "test", null, new String[]{"TABLE"})
                .count());

        assertEquals(0,collect(null, null, null, new String[]{"VIEW"})
                .count());
    }

    @Test
    void byPatternMatching() {
        val tables = collect(null, "%", "c%", null)
                .map(k-> k.name())
                .collect(Collectors.toSet());
        assertEquals(Set.of("cities"), tables);
    }

}