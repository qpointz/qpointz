package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class BinaryColumnVectorReader extends VectorColumnReader {

    public BinaryColumnVectorReader(Vector vector) {
        super(vector);
    }

    public byte[] getVectorValue(int rowIdx) {
        return this.getVectorBytes(rowIdx);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public byte[] getBytes(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public InputStream getBinaryStream(int rowIdx) {
        return new ByteArrayInputStream(this.getVectorValue(rowIdx));
    }


}
