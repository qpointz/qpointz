package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;

public class FloatColumnVectorReader extends VectorColumnReader {
    public FloatColumnVectorReader(Vector vector) {
        super(vector);
    }

    private Float getVectorValue(int rowIdx) {
        return this.getVectorFloat(rowIdx);
    }

    @Override
    public Object getObject(int rowIdx) {
        return getVectorValue(rowIdx);
    }
}
