package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.I32Physical;

public final class IntervalDayLogical implements LogicalType<Integer, I32Physical> {

    public IntervalDayLogical() {}

    public static IntervalDayLogical INSTANCE = new IntervalDayLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I32Physical getPhysicalType() {
        return I32Physical.INSTANCE;
    }
}