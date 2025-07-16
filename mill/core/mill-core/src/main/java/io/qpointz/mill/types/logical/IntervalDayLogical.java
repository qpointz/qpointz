package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.I32Physical;

public final class IntervalDayLogical implements LogicalType<Integer, I32Physical> {

    public static final IntervalDayLogical INSTANCE = new IntervalDayLogical();
    public static final int DEFAULT_PRECISION = 4;

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I32Physical getPhysicalType() {
        return I32Physical.INSTANCE;
    }

    public int precision() {
        return DEFAULT_PRECISION;
    }

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.INTERVAL_DAY;
    }
}