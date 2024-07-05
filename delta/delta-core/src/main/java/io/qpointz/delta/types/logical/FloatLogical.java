package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.FP32Physical;

public final class FloatLogical implements LogicalType<Float, FP32Physical> {

    private FloatLogical() {}

    public static final FloatLogical INSTANCE = new FloatLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public FP32Physical getPhysicalType() {
        return FP32Physical.INSTANCE;
    }
}
