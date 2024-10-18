package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.TestVectorData;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.BinaryLogical;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class BinaryColumnVectorReaderTest {

    private byte[] a1 = new byte[] {100, 12, -100, 12, (byte)234, (byte)-234};
    private byte[] a2 = new byte[] {};
    private byte[] a3 = Base64.getDecoder().decode("3wWJVd0GE1CELMW6eiBFa2irYE3cLZIU0Z2Kx+yNctvVQaFcbPLhJXVGgzHP7Y/3zWbmkeMUAOvi" +
            "CTSJp6DPYjwEHt4cXihT8KNZs2sdr9Kqs0ntAdnSW8H3So39nm7Bf5e+qjODSWErbZZB1cMgNP4b" +
            "k8lt+dUwjOePQPBGAnb88WNmv8mQurqSq//TPjfE4SS7UuetCByRc6ZitY0dp7W/r+UtzrIwMpc3" +
            "Ret74nBS1nzNrc31xOUUD0WN7b5Vl+HyY1kwUxAmFiaLMZyzOqyX+XdFGv+1L5BlKKHMSoflPg==");

    private Vector vector =new TestVectorData.LogicalTypeVectorData<>("Binary", BinaryLogical.INSTANCE, TestVectorData::bytesToString,
            new byte[][] {
            a1,
            null,
            a2,
            a3}).getVectorProducer().vectorBuilder().build();

    private BinaryColumnVectorReader reader = new BinaryColumnVectorReader(vector);

    @Test
    void readNulls() {
        assertTrue(reader.isNull(1));
        assertFalse(reader.isNull(0));
        assertFalse(reader.isNull(2));
    }

    @Test
    public void readBytes() {
        assertArrayEquals(a1, reader.getBytes(0));
        assertArrayEquals(a2, reader.getBytes(2));
    }

    @Test
    public void readBytesStream() throws IOException {
        val buff = new byte[a1.length];
        val stream = reader.getBinaryStream(0).read(buff);
        assertArrayEquals(a1, buff);
    }

    @Test
    public void readObject() {
        assertArrayEquals(a1, (byte[])reader.getObject(0));
    }


}