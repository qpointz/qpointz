package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.conversion.LocalDateTimeToEpochMilli;
import io.qpointz.mill.types.physical.I64Physical;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public final class TimestampLogical implements LogicalType<Long, I64Physical> {

    private TimestampLogical() {}

    public static final TimestampLogical INSTANCE = new TimestampLogical();

    public static final LocalDateTimeToEpochMilli DEFAULT_CONVERTER = new LocalDateTimeToEpochMilli();

    public static final LocalDateTime MAX_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999999999);
    public static final long MAX = DEFAULT_CONVERTER.to(MAX_DATE);

    public static final LocalDateTime MIN_DATE = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
    public static final long MIN = DEFAULT_CONVERTER.to(MIN_DATE);


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
        return LogicalDataType.LogicalDataTypeId.TIMESTAMP;
    }



    public static LocalDateTime fromPhysical(Long k) {
        return DEFAULT_CONVERTER.from(k);
    }

    public static Long toPhysical(LocalDateTime utc) {
        return DEFAULT_CONVERTER.to(utc);
    }



}
