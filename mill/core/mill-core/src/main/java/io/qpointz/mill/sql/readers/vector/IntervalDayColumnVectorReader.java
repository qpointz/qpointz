package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;

public class IntervalDayColumnVectorReader extends VectorColumnReader {
    public IntervalDayColumnVectorReader(Vector vector) {
        super(vector);
    }

    @Override
    public Object getObject(int rowIdx) {
        throw notSupported();
    }
}
