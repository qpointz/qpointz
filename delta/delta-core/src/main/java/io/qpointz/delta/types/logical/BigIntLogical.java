package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.I64Physical;
import io.qpointz.delta.types.physical.PhysicalType;
import io.qpointz.delta.vectors.VectorProducer;

public final class BigIntLogical implements LogicalType<Long, I64Physical> {

    public BigIntLogical() {}

    public static BigIntLogical INSTANCE = new BigIntLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I64Physical getPhysicalType() {
        return I64Physical.INSTANCE;
    }
}
