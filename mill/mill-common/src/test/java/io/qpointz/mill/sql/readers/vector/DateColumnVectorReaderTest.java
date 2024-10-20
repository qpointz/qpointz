package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.TestVectorData;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.DateLogical;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateColumnVectorReaderTest {

    private Vector vector = new TestVectorData.LogicalTypeVectorData<>("Date", DateLogical.INSTANCE, k -> DateLogical.fromPhysical(k).toString(),
            new Long[]{
                    DateLogical.toPhysical(LocalDate.EPOCH),
                    null,
                    DateLogical.MIN_DAYS,
                    DateLogical.MAX_DAYS
            }).getVectorProducer().vectorBuilder().build();

    private DateColumnVectorReader reader = new DateColumnVectorReader(vector);

    @Test
    void readNulls() {
        assertTrue(reader.isNull(1));
        assertFalse(reader.isNull(0));
    }

    @Test
    void readDate() {
        assertEquals(Date.valueOf(LocalDate.EPOCH), reader.getDate(0));
    }

    @Test
    void readObject() {
        assertEquals(Date.valueOf(LocalDate.ofEpochDay(DateLogical.MAX_DAYS)), reader.getObject(3));
    }

    @Test
    void readLong() {
        assertEquals(0, reader.getLong(0));
        assertEquals(DateLogical.MIN_DAYS, reader.getLong(2));
        assertEquals(DateLogical.MAX_DAYS, reader.getLong(3));
    }



}