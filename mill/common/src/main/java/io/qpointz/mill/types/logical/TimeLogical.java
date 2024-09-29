package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.conversion.LocalTimeToNanoConverter;
import io.qpointz.mill.types.physical.I64Physical;

import java.time.LocalTime;

public final class TimeLogical implements LogicalType<Long, I64Physical> {

    public static final Long MIN = LocalTime.MIN.toNanoOfDay();
    public static final Long MAX = LocalTime.MAX.toNanoOfDay();

    private TimeLogical() {}

    public static final TimeLogical INSTANCE = new TimeLogical();

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
        return LogicalDataType.LogicalDataTypeId.TIME;
    }

    public static LocalTimeToNanoConverter DEFAULT_CONVERTER = new LocalTimeToNanoConverter();

    public static Long toPhysical(LocalTime of) {

        return DEFAULT_CONVERTER.to(of);
    }

    public static LocalTime fromPhysical(Long time) {
        return DEFAULT_CONVERTER.from(time);
    }


}
