package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.conversion.LocalDateToEpochConverter;
import io.qpointz.mill.types.physical.I64Physical;


import java.time.LocalDate;

public final class DateLogical implements LogicalType<Long, I64Physical> {

    private DateLogical() {}

    public static final DateLogical INSTANCE = new DateLogical();

    public static final long MIN_DAYS = -719162L; //lowe bound date 01-01-0001
    public static final long MAX_DAYS = 2932896L; //high bound date 31-12-9999

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I64Physical getPhysicalType() {
        return I64Physical.INSTANCE;
    }

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.DATE;
    }

    public static final LocalDateToEpochConverter DEFAULT_CONVERTER = new LocalDateToEpochConverter();

    public static Long toPhysical(LocalDate localDate) {
        return DEFAULT_CONVERTER.to(localDate);
    }
    public static LocalDate fromPhysical(Long value) {
        return DEFAULT_CONVERTER.from(value);
    }

}
