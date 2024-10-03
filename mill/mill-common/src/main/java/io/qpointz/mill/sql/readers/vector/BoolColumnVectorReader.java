package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;

public class BoolColumnVectorReader extends VectorColumnReader {

    public BoolColumnVectorReader(Vector vector) {
        super(vector);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    private Boolean getVectorValue(int rowIdx) {
        return this.getVectorBoolean(rowIdx);
    }
}
