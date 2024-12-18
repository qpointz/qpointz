package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;

public class TinyIntColumnVectorReader extends VectorColumnReader {
    public TinyIntColumnVectorReader(Vector vector) {
        super(vector);
    }

    private byte getVectorValue(int rowIdx) {
        return this.getVectorInt(rowIdx).byteValue();
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public int getInt(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public long getLong(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }
}
