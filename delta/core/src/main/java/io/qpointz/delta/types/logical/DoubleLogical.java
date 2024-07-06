package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.FP64Physical;

public final class DoubleLogical implements LogicalType<Double, FP64Physical> {

    private DoubleLogical() {}

    public static final DoubleLogical INSTANCE = new DoubleLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public FP64Physical getPhysicalType() {
        return FP64Physical.INSTANCE;
    }
}
