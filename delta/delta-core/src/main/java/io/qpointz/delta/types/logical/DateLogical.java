package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.I32Physical;

public final class DateLogical implements LogicalType<Integer, I32Physical> {

    private DateLogical() {}

    public static final DateLogical INSTANCE = new DateLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I32Physical getPhysicalType() {
        return I32Physical.INSTANCE;
    }
}
