package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.Vector;
import io.substrait.proto.Type;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntegerTypeHandlerBaseTest {


    @Test
    void testI32read() {
        val iv = Vector.Int32Vector.newBuilder()
                .addAllValues(List.of(1,2,3))
                .addAllNulls(List.of(false, true, false));
        val v = Vector.newBuilder().setInt32Vector(iv).build();
        val ith = new IntegerTypeHandler(Type.Nullability.NULLABILITY_NULLABLE);
        assertEquals(1, ith.read(v, 0));
        assertTrue(ith.isNull(v,1));
        assertFalse(ith.isNull(v, 2));
    }

}