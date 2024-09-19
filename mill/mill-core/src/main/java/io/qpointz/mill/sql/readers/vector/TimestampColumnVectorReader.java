package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.TimestampLogical;

import java.time.LocalDateTime;

public class TimestampColumnVectorReader extends ConvertingVectorColumnReader<LocalDateTime, Long> {

    public TimestampColumnVectorReader(Vector vector) {
        super(vector, TimestampLogical.DEFAULT_CONVERTER);
    }

    @Override
    protected Long getVectorValue(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }
}
