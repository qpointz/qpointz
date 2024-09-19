package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.types.conversion.ValueConverter;
import io.qpointz.mill.types.logical.DateLogical;

import java.time.LocalDate;

public class DateColumnVectorReader extends ConvertingVectorColumnReader<LocalDate, Long> {

    public DateColumnVectorReader(Vector vector) {
        super(vector, DateLogical.DEFAULT_CONVERTER);
    }

    @Override
    protected Long getVectorValue(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }
}
