package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.TestVectorData;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.BigIntLogical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BigIntColumnVectorReaderTest {

    private Vector vector = new TestVectorData.LogicalTypeVectorData<>("BigInt", BigIntLogical.INSTANCE, Object::toString,
            new Long[]{1L, null, Long.MIN_VALUE, Long.MAX_VALUE})
            .getVectorProducer()
            .vectorBuilder()
            .build();

    private BigIntColumnVectorReader reader = new BigIntColumnVectorReader(vector);

    @Test
    void readLong() {
        assertEquals(1L, reader.getLong(0));
    }

    @Test
    void readNulls() {
        assertTrue(reader.isNull(1));
        assertFalse(reader.isNull(0));
    }

    @Test
    void readByte() {
        assertEquals((byte)1, reader.getByte(0));
    }

    @Test
    void readShort() {
        assertEquals((short) 1, reader.getShort(0));
    }

    @Test
    void readInt() {
        assertEquals(1, reader.getInt(0));
    }

    @Test
    void readObject() {
        assertEquals(1L, reader.getObject(0));
    }

    @Test
    void readDouble() {
        assertEquals(1D, reader.getDouble(0));
    }

    @Test
    void readFloat() {
        assertEquals(1F, reader.getFloat(0));
    }


}