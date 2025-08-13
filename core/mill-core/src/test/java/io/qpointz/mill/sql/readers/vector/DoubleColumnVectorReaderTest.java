package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.TestVectorData;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.DoubleLogical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleColumnVectorReaderTest {

    private Vector vector =  new TestVectorData.LogicalTypeVectorData<>("Double", DoubleLogical.INSTANCE, Object::toString,
                    new Double[] {-23423423.234D, Double.MIN_VALUE, Double.MAX_VALUE, null}
            ).getVectorProducer().vectorBuilder().build();

    private DoubleColumnVectorReader reader = new DoubleColumnVectorReader(vector);

    @Test
    void readNulls() {
        assertFalse(reader.isNull(0));
        assertFalse(reader.isNull(1));
        assertFalse(reader.isNull(2));
        assertTrue(reader.isNull(3));
    }

    @Test
    void readLong() {
        assertEquals(-23423423L, reader.getLong(0));
    }


    @Test
    void readByte() {
        assertEquals(65, reader.getByte(0));
    }

    @Test
    void readShort() {
        assertEquals(-27071, reader.getShort(0));
    }

    @Test
    void readInt() {
        assertEquals(-23423423, reader.getInt(0));
    }

    @Test
    void readObject() {
        assertEquals(-23423423.234D, reader.getObject(0));
    }

    @Test
    void readDouble() {
        assertEquals(-23423423.234D, reader.getDouble(0));
    }

    @Test
    void readFloat() {
        assertEquals(-23423423.234F, reader.getFloat(0));
    }

}