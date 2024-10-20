package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;
import io.qpointz.mill.types.logical.IntervalYearLogical;

public class IntervalYearColumnVectorReader extends VectorColumnReader {
    public IntervalYearColumnVectorReader(Vector vector) {
        super(vector);
    }

    @Override
    public Object getObject(int rowIdx) {
        throw notSupported();
    }

}
