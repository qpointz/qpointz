package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;

public class SmallIntColumnVectorReader extends VectorColumnReader {

    public SmallIntColumnVectorReader(Vector vector) {
        super(vector);
    }

    private short getVectorValue(int rowIdx) {
        return this.getVectorInt(rowIdx).shortValue();
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }
}
