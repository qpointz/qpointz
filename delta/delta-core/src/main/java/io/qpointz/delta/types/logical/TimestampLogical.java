package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.I64Physical;

public final class TimestampLogical implements LogicalType<Long, I64Physical> {

    private TimestampLogical() {}

    public static final TimestampLogical INSTANCE = new TimestampLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I64Physical getPhysicalType() {
        return I64Physical.INSTANCE;
    }
}
