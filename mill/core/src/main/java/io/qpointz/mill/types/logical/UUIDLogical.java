package io.qpointz.mill.types.logical;

import io.qpointz.mill.types.physical.BytesPhysical;

public final class UUIDLogical implements LogicalType<byte[], BytesPhysical> {

    private UUIDLogical() {}

    public static final UUIDLogical INSTANCE = new UUIDLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public BytesPhysical getPhysicalType() {
        return BytesPhysical.INSTANCE;
    }
}
