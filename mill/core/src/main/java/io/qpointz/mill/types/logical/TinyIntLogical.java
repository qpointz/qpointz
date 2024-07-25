package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.I32Physical;

public final class TinyIntLogical implements LogicalType<Integer, I32Physical> {

    private TinyIntLogical() {}

    public static final TinyIntLogical INSTANCE = new TinyIntLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I32Physical getPhysicalType() {
        return I32Physical.INSTANCE;
    }

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.TINY_INT;
    }

    public Integer valueFrom(Short val) {
        return val.intValue();
    }

}
