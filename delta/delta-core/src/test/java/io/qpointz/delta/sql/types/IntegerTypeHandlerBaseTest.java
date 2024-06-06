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
        val iv = Vector.I32Vector.newBuilder()
                .addAllValues(List.of(1,2,3));
        val nulls = Vector.NullsVector.newBuilder().addAllNulls(List.of(false, true, false));
        val v = Vector.newBuilder().setI32Vector(iv).setNulls(nulls).build();
        val ith = new IntegerTypeHandler(Type.Nullability.NULLABILITY_NULLABLE);
        assertEquals(1, ith.read(v, 0));
        assertTrue(ith.isNull(v,1));
        assertFalse(ith.isNull(v, 2));
    }

}