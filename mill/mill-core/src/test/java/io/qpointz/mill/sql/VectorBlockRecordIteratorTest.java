package io.qpointz.mill.sql;

import io.qpointz.mill.proto.Field;
import io.qpointz.mill.proto.VectorBlock;
import lombok.val;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.FileInputStream;
import java.io.IOException;

class VectorBlockRecordIteratorTest {

    VectorBlock getBlock(String casePath) throws IOException {
        String block = "../test/" + casePath + ".bin";
        val stream = new FileInputStream(block);
        return VectorBlock.parseFrom(stream);
    }

    VectorBlockRecordIterator getReader(String casePath) throws IOException {
        val block = getBlock(casePath);
        return VectorBlockRecordIterator.of(block);
    }

    @Test
    void readCase() throws IOException {
        val block = getBlock("messages/logical-types");
        assertNotNull(block);
    }

    @Test
    void iterateBlock() throws IOException {
        var block = getBlock("messages/logical-types");
        val size = block.getVectorSize();
        int cnt = 0;
        val reader = VectorBlockRecordIterator.of(block);
        while (reader.next()) {
            cnt++;
        }
        assertEquals(size, cnt);
    }

    @Test
    void allColumnsReadable() throws IOException {
        var block = getBlock("messages/logical-types");
        val reader = VectorBlockRecordIterator.of(block);
        val columnCnt = block.getSchema().getFieldsCount();
        val names = block.getSchema().getFieldsList().stream()
                .map(Field::getName)
                .toList();

        while (reader.next())
            for(int i=0;i<columnCnt;i++) {
                val idx = i;
                val typeId = block.getSchema().getFieldsList().get(i).getType().getType().getTypeId();
                assertDoesNotThrow(()-> reader.isNull(idx), String.format("'isNull' value for idx:%s, name:%s, type:%s fails", idx, names.get(idx), typeId));
                if (!reader.isNull(idx)) {
                    assertDoesNotThrow(() -> reader.getObject(idx), String.format("'getObject' for idx:%s, name:%s, type:%s fails", idx, names.get(idx), typeId));
                }
                assertEquals(idx, reader.getColumnIndex(names.get(idx)));
            }
    }




}