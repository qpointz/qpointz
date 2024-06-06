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
        producer.append(23);
        producer.append(34);
        producer.appendNull();
        val builder = Vector.newBuilder();
        producer.addToVector(builder);
        val vector = builder.build();
        val nulls = vector.getNulls().getNullsList();
        val values = vector.getI32Vector().getValuesList();
        assertEquals(List.of(23,34, producer.getNullValue()), values);
        assertEquals(List.of(false, false, true), nulls);
    }

    @Test
    void addAllValues() {
        val producer = StringPhysical.INSTANCE.createVectorProducer();
        producer.appendAll(List.of("a","b"));
        producer.appendNulls(2);
        val builder = Vector.newBuilder();
        producer.addToVector(builder);
        val vector = builder.build();
        val values = vector.getStringVector().getValuesList();
        val nulls = vector.getNulls().getNullsList();
        assertEquals(List.of("a","b", producer.getNullValue(), producer.getNullValue()), values);
        assertEquals(List.of(false, false, true, true), nulls);
    }

    @Test
    void resetProducer() {
        val producer = I32Physical.INSTANCE.createVectorProducer();
        producer.appendAll(List.of(1,2));
        producer.appendNulls(2);
        producer.reset();
        producer.appendAll(List.of(3,4));
        producer.appendNull();
        val vector = producer.asVectorBuilder().build();
        val values = vector.getI32Vector().getValuesList();
        val nulls = vector.getNulls().getNullsList();
        assertEquals(List.of(3,4, producer.getNullValue()), values);
        assertEquals(List.of(false, false, true), nulls);
    }

}