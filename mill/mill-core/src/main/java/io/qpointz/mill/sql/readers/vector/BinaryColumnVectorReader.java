package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;

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
}
