package io.qpointz.mill.vectors;

import io.qpointz.mill.types.logical.IntLogical;
import io.qpointz.mill.types.logical.StringLogical;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

class ObjectToVectorProducerTest {

    public record TestRecord(String name, Integer count){}

    private static List<TestRecord> getTestRecords() {
        return List.of(
                new TestRecord("Hallo", -2),
                new TestRecord("", 0),
                new TestRecord("World", 12)
        );
    }

    private static List<ObjectToVectorProducer.MapperInfo<TestRecord,?>> getMappings() {
        return List.of(
                mapper("NAME", StringLogical.INSTANCE, k -> k.name.equals("") ? Optional.empty() : Optional.of(k.name)),
                mapper("CNT", IntLogical.INSTANCE, k -> k.count >= 0 ? Optional.of(k.count) : Optional.empty())
        );
    }


    @Test
    void vanila() {
        final var ovp = getMappings();
        final var records = getTestRecords();

        val vb = new ObjectToVectorProducer(ovp)
                .fromCollection(records);

        assertTrue(vb.getVectorSize()>0);
        assertTrue(vb.getVectors(0).getStringVector().getValuesCount()==vb.getVectorSize());
        assertTrue(vb.getVectors(1).getI32Vector().getValuesCount()==vb.getVectorSize());
    }

    @Test
    void createResultSet() throws SQLException {
        final var ovp = getMappings();
        final var records = getTestRecords();
        val rs = ObjectToVectorProducer.resultSet(ovp,records);
        var recCnt=0;
        while (rs.next()) {
            rs.getString("NAME");
            rs.getInt("CNT");
            recCnt++;
        }
        assertEquals(3,recCnt);
    }


}