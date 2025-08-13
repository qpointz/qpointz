package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.DateLogical;

import java.sql.Date;
import java.time.LocalDate;

public class DateColumnVectorReader extends ConvertingVectorColumnReader<LocalDate, Long> {

    public DateColumnVectorReader(Vector vector) {
        super(vector, DateLogical.DEFAULT_CONVERTER);
    }

    @Override
    protected Long getVectorValue(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }

    @Override
    public Date getDate(int rowIdx) {
        return Date.valueOf(super.getValue(rowIdx));
    }

    @Override
    public long getLong(int rowIdx) {
        return super.getVectorLong(rowIdx);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getDate(rowIdx);
    }
}