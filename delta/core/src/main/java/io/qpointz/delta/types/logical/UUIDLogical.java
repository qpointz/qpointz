package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.BytesPhysical;

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
