package io.qpointz.mill.vectors;

import io.qpointz.mill.types.physical.StringPhysical;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappingVectorProducerTest {

    @Test
    void trivial() {
        val producer = StringPhysical.INSTANCE.createVectorProducer();
        val mapper = MappingVectorProducer.DelegatingMappingVectorProducer.<Integer,String>createProducer(producer,
                Object::toString, k -> k == 0);
        val vals = List.of(0,1,2,3);
        val nulls = List.of(true,false,false,false);
        mapper.append(vals,nulls);
        val vector = mapper.vectorBuilder()
                        .build();
        assertEquals(4, vector.getStringVector().getValuesList().size());
        assertEquals(List.of(true, false, false, false), vector.getNulls().getNullsList());
    }



}