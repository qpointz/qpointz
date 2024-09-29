package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;

public class DoubleColumnVectorReader extends VectorColumnReader {
    public DoubleColumnVectorReader(Vector vector) {
        super(vector);
    }

    private Double getVectorValue(int rowIdx) {
        return this.getVectorDouble(rowIdx);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }
}
