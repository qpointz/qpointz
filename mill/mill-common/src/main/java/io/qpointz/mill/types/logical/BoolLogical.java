package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.BoolPhysical;

public final class BoolLogical implements LogicalType<Boolean, BoolPhysical> {

    private BoolLogical() {}

    public static final BoolLogical INSTANCE = new BoolLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public BoolPhysical getPhysicalType() {
        return BoolPhysical.INSTANCE;
    }

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.BOOL;
    }
}
