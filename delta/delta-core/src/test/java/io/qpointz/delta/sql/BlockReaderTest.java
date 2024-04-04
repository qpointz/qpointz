package io.qpointz.delta.sql;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class BlockReaderTest extends BaseJdbcTest {

    private Connection conn = createDb("rsmeta", "/sql/simple.sql");

    @Test
    void readBlocks() {
        val rs = execute(conn, "SELECT * FROM TEST");
        val br = new BlockReader(rs, 10);
        val list = StreamSupport.stream(Spliterators.spliteratorUnknownSize(br, Spliterator.ORDERED), false).toList();
        assertTrue(list.size()>2);
    }

}