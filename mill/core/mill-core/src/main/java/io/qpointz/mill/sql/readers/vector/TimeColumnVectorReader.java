package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.TimeLogical;

import java.time.LocalTime;

public class TimeColumnVectorReader extends ConvertingVectorColumnReader<LocalTime, Long> {

    public TimeColumnVectorReader(Vector vector) {
        super(vector, TimeLogical.DEFAULT_CONVERTER);
    }

    @Override
    protected Long getVectorValue(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }
}
