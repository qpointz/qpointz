package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.I64Physical;

public final class BigIntLogical implements LogicalType<Long, I64Physical> {

    public static final BigIntLogical INSTANCE = new BigIntLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I64Physical getPhysicalType() {
        return I64Physical.INSTANCE;
    }

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.BIG_INT;
    }
}
