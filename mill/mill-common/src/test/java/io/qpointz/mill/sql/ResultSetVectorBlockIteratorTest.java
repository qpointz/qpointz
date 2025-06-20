package io.qpointz.mill.sql;

import io.qpointz.mill.vectors.VectorBlockIterator;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetVectorBlockIteratorTest {

    private H2Db db;

    public ResultSetVectorBlockIteratorTest() throws Exception {
        this.db = H2Db.createFromResource("sql/simple.sql");
    }

    public void close() throws Throwable {
        db.close();
    }

    private VectorBlockIterator sql(String sql, int fetchSize, List<String> names) {
        var rs = db.query(sql);
        return new ResultSetVectorBlockIterator(rs, fetchSize, names);
    }

    @Test
    public void iterateOver() {
        val iter = sql("select * from TEST", 33, null);
        assertTrue(iter.hasNext());
        var rowCount = 0;
        var blockCount = 0;
        while(iter.hasNext()) {
            val block = iter.next();
            assertNotNull(iter.schema());
            blockCount++;
            rowCount+=block.getVectorSize();
        }
        assertEquals(1000, rowCount);
        assertEquals(31, blockCount);
    }

    @Test
    public void rewriteNames() {
        val iter = sql("select id, first_name from TEST", 33, List.of("NEWID", "NEWNAME"));
        assertTrue(iter.hasNext());
        val schema = iter.schema().getFieldsList();
        assertEquals("NEWID", schema.get(0).getName());
        assertEquals("NEWNAME", schema.get(1).getName());
    }

    @Test
    public void emptyNames() {
        val iter = sql("select ID, FIRST_NAME from TEST", 33, List.of());
        assertTrue(iter.hasNext());
        val schema = iter.schema().getFieldsList();
        assertEquals("ID", schema.get(0).getName());
        assertEquals("FIRST_NAME", schema.get(1).getName());
    }

    @Test
    public void nullNames() {
        val iter = sql("select ID, FIRST_NAME from TEST", 33, null);
        assertTrue(iter.hasNext());
        val schema = iter.schema().getFieldsList();
        assertEquals("ID", schema.get(0).getName());
        assertEquals("FIRST_NAME", schema.get(1).getName());
    }



}