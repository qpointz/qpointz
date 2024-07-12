package io.qpointz.mill.types.logical;

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

    public Integer valueFrom(Short val) {
        return val.intValue();
    }

}
