package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.BytesPhysical;
import io.qpointz.delta.vectors.VectorProducer;

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

}
