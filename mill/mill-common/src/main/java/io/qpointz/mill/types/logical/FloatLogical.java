package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.FP32Physical;

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

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.FLOAT;
    }
}
