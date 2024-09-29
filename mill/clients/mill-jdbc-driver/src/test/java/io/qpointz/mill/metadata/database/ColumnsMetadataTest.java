package io.qpointz.mill.metadata.database;

import io.qpointz.mill.InProcessTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ColumnsMetadataTest extends InProcessTest {

    private Stream<ColumnsMetadata.ColumnRecord> collect(String catalogPattern, String schemaPattern, String tableNamePattern, String columnNamePattern) {
        val con = this.createConnection();
        return new ColumnsMetadata(con, catalogPattern, schemaPattern, tableNamePattern, columnNamePattern)
                .getMetadata().stream();
    }

    @Test
    void trivia() {
        assertTrue(collect(null, null, null, null)
                .count() > 0);

    }

    @Test
    void byColumnName() {
        assertEquals(1, collect(null, "test", "TEST", "FIRST_NAME")
                .count());

    }



}