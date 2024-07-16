package io.qpointz.mill.types.logical;

import io.qpointz.mill.types.physical.I32Physical;

public final class IntLogical implements LogicalType<Integer, I32Physical> {

    private IntLogical() {}

    public static final IntLogical INSTANCE = new IntLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I32Physical getPhysicalType() {
        return I32Physical.INSTANCE;
    }
}
