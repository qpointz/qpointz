package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;

public class IntColumnVectorReader extends VectorColumnReader {
    public IntColumnVectorReader(Vector vector) {
        super(vector);
    }

    private Integer getVectorValue(int rowIdx) {
        return this.getVectorInt(rowIdx);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public int getInt(int rowIdx) {
        return this.getVectorInt(rowIdx);
    }

    @Override
    public long getLong(int rowIdx) {
        return this.getInt(rowIdx);
    }
}
