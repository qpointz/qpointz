package io.qpointz.delta.sql;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetVectorBlockIteratorTest {

    private H2Db db;

    public ResultSetVectorBlockIteratorTest() throws Exception {
        this.db = H2Db.createFromResource("sql/simple.sql");
    }

    public void close() throws Throwable {
        db.close();
    }

    private VectorBlockIterator sql(String sql, int batchSize) {
        var rs = db.query(sql);
        return VectorBlockIterators.fromResultSet(rs, batchSize);
    }

    @Test
    public void iterateOver() {
        val iter = sql("select * from TEST", 33);
        assertTrue(iter.hasNext());
        var rowCount = 0;
        var blockCount = 0;
        while(iter.hasNext()) {
            val block = iter.next();
            assertNotNull(iter.schema());
            blockCount++;
            rowCount+=block.getVectorSize();
        }
        assertEquals(100, rowCount);
        assertEquals(4, blockCount);
    }

}