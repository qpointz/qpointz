package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;

public class StringColumnVectorReader extends VectorColumnReader {

    public StringColumnVectorReader(Vector vector) {
        super(vector);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorString(rowIdx);
    }

    @Override
    public String getString(int rowIdx) {
        return this.getVectorString(rowIdx);
    }
}
