package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.BytesPhysical;

public final class BinaryLogical implements LogicalType<byte[], BytesPhysical> {

    public static final BinaryLogical INSTANCE = new BinaryLogical();

    private BinaryLogical() {
    }

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public BytesPhysical getPhysicalType() {
        return BytesPhysical.INSTANCE;
    }

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.BINARY;
    }

}
