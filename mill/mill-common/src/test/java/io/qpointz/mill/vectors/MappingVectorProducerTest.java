package io.qpointz.mill.vectors;

import io.qpointz.mill.types.physical.StringPhysical;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappingVectorProducerTest {

    /*
    public void shift(int x, int y) {
        long l = (((long)x) << 32) | (y & 0xffffffffL);
        int x1 = (int)(l >> 32);
        int y1 = (int)l;
        assertEquals(x, x1);
        assertEquals(y, y1);
    }

    @Test
    public void testa() {
        shift(0,0);
        shift(Integer.MAX_VALUE,Integer.MAX_VALUE);
        shift(Integer.MIN_VALUE,Integer.MIN_VALUE);
        shift(Integer.MAX_VALUE,Integer.MIN_VALUE);
        shift(Integer.MIN_VALUE,Integer.MAX_VALUE);
        shift(-30,-20);
        shift(20,30);
        shift(30,-20);
        shift(-30,20);
    }

    @Test
    public void period() {
        val dmin = Duration.ofMillis(Long.MAX_VALUE);
        val pmin = Period.ZERO.plus(dmin);
    }*/


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