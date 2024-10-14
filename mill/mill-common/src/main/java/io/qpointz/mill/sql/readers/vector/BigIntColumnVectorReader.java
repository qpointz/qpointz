package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;

public class BigIntColumnVectorReader extends VectorColumnReader {
    public BigIntColumnVectorReader(Vector vector) {
        super(vector);
    }

    private Long getVectorValue(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public long getLong(int rowIdx) {
        return (long) this.getInt(rowIdx);
    }
}
