package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.I32Physical;

public final class SmallIntLogical implements LogicalType<Integer, I32Physical> {

    private SmallIntLogical() {}

    public static final SmallIntLogical INSTANCE = new SmallIntLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I32Physical getPhysicalType() {
        return I32Physical.INSTANCE;
    }
}
