package io.qpointz.delta.vectors;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.types.physical.I32Physical;
import io.qpointz.delta.types.physical.StringPhysical;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalTypeVectorProducerTest {

    @Test
    void vanilaProducer() {
        val producer = I32Physical.INSTANCE.createVectorProducer();
        producer.append(23, false);
        producer.append(34, false);
        producer.append(null, true);
        val vector = producer.vectorBuilder().build();
        val nulls = vector.getNulls().getNullsList();
        val values = vector.getI32Vector().getValuesList();
        assertEquals(List.of(23,34, producer.getNullValue()), values);
        assertEquals(List.of(false, false, true), nulls);
    }

    @Test
    void addAllValues() {
        val producer = StringPhysical.INSTANCE.createVectorProducer();
        producer.append(List.of("a","b"), List.of(false, false));
        producer.append(List.of("null", "null"), List.of(true, true) );
        val vector = producer.vectorBuilder().build();
        val values = vector.getStringVector().getValuesList();
        val nulls = vector.getNulls().getNullsList();
        assertEquals(List.of("a","b", producer.getNullValue(), producer.getNullValue()), values);
        assertEquals(List.of(false, false, true, true), nulls);
    }

    @Test
    void resetProducer() {
        val producer = I32Physical.INSTANCE.createVectorProducer();
        producer.append(List.of(1,2), List.of(false, false));
        producer.reset();
        producer.append(List.of(3,4), List.of(false, false));
        producer.append(null, true);
        val vector = producer.vectorBuilder().build();
        val values = vector.getI32Vector().getValuesList();
        val nulls = vector.getNulls().getNullsList();
        assertEquals(List.of(3,4, producer.getNullValue()), values);
        assertEquals(List.of(false, false, true), nulls);
    }

}