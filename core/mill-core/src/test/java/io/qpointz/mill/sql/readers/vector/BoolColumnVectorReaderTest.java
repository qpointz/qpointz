package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.TestVectorData;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.BoolLogical;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoolColumnVectorReaderTest {

    private Vector vector = //Boolean
            new TestVectorData.LogicalTypeVectorData<>("Boolean", BoolLogical.INSTANCE, Object::toString,
                    new Boolean[] {
                            true,
                            null,
                            false,
                            null
                    }).getVectorProducer().vectorBuilder().build();

    private BoolColumnVectorReader reader = new BoolColumnVectorReader(vector);

    @Test
    void readNulls() {
        assertTrue(reader.isNull(1));
        assertFalse(reader.isNull(0));
    }

    @Test
    void readBoolean() {
        assertTrue(reader.getBoolean(0));
        assertFalse(reader.getBoolean(2));
    }

    @Test
    void readObject() {
        assertEquals(true, reader.getBoolean(0));
    }


}