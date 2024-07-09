package io.qpointz.mill.types.logical;

import io.qpointz.mill.types.physical.I64Physical;

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
