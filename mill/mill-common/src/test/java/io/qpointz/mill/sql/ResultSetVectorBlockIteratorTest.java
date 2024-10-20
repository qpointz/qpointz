package io.qpointz.mill.sql;

import io.qpointz.mill.vectors.VectorBlockIterator;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
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

    private VectorBlockIterator sql(String sql, int fetchSize) {
        var rs = db.query(sql);
        return new ResultSetVectorBlockIterator(rs, fetchSize);
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
        assertEquals(1000, rowCount);
        assertEquals(31, blockCount);
    }

}