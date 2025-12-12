package io.qpointz.mill.ai.nlsql.processors;

import io.qpointz.mill.ai.nlsql.ChatEventProducer;
import io.qpointz.mill.ai.nlsql.components.DefaultValueMapper;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapValueProcessorTest {


    private MapValueProcessor processor() {
        return new MapValueProcessor(new DefaultValueMapper(), ChatEventProducer.DEFAULT);
    }

    @Test
    void noSqlReturnSameResult() {
        val p = Map.<String, Object>of("k", "b");
        assertEquals(p, processor().process(p));
    }

    @Test
    void blankSqlReturnSameResult() {
        val p = Map.<String, Object>of("sql", "");
        assertEquals(p, processor().process(p));
    }

    @Test
    void noValueMapping() {
        val p = Map.<String, Object>of(
                "sql", "select * from TABLE");
        assertEquals(p, processor().process(p));
    }

    @Test
    void trivialMapping() {
        val p = Map.of(
                "sql",
                "SELECT * FROM `MONETA`.`CLIENTS` WHERE `SEGMENT` = \"@{MONETA.CLIENTS.SEGMENT:segment_regular}\"",
                "value-mapping", List.of(
                        Map.of(
                                "placeholder","segment_regular",
                                "target", "MONETA.CLIENTS.SEGMENT",
                                "display","regular",
                                "resolved-value", "regular",
                                "type", "string",
                                "kind", "constant",
                                "meaning", "Client segment is regular"
                        )));
        val o = processor().process(p);
        val sql = o.get("sql");
        assertEquals("SELECT * FROM `MONETA`.`CLIENTS` WHERE `SEGMENT` = \"regular\"", sql);
    }

//
//
//    {
//        }

}