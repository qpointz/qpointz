package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.types.logical.TimestampTZLogical;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class TimestampTzColumnVectorReader extends ConvertingVectorColumnReader<ZonedDateTime, Long> {

    public TimestampTzColumnVectorReader(Vector vector) {
        super(vector, TimestampTZLogical.DEFAULT_CONVERTER);
    }


    @Override
    protected Long getVectorValue(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }


    @Override
    public Timestamp getTimestamp(int rowIdx) {
        return Timestamp.from(getValue(rowIdx).toInstant());
    }
}
