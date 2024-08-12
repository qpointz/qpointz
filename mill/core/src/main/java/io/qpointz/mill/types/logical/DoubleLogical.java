package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.FP64Physical;

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

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.DOUBLE;
    }
}
