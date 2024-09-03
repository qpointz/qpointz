package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.conversion.LocalDateTimeToEpochMilli;
import io.qpointz.mill.types.conversion.ZonedDateTimeToEpochMillis;
import io.qpointz.mill.types.physical.I64Physical;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class TimestampTZLogical implements LogicalType<Long, I64Physical> {

    private TimestampTZLogical() {}

    public static final TimestampTZLogical INSTANCE = new TimestampTZLogical();

    private static final ZonedDateTimeToEpochMillis DEFAULT_CONVERTER = new ZonedDateTimeToEpochMillis();

    private static final ZoneId REFZONE = ZoneId.of("UTC");

    public static final ZonedDateTime MAX_DATE = ZonedDateTime.of(9999, 12, 31, 23, 59, 59, 999999999, REFZONE);
    public static final long MAX = DEFAULT_CONVERTER.to(MAX_DATE);

    public static final ZonedDateTime MIN_DATE = ZonedDateTime.of(1, 1, 1, 0, 0, 0, 0, REFZONE);
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
        return LogicalDataType.LogicalDataTypeId.TIMESTAMP_TZ;
    }

    public static ZonedDateTime fromPhysical(Long k) {
        return DEFAULT_CONVERTER.from(k);
    }

    public static Long toPhysical(ZonedDateTime utc) {
        return DEFAULT_CONVERTER.to(utc);
    }

}
