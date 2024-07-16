package io.qpointz.mill.types.logical;


import io.qpointz.mill.types.physical.I32Physical;

public final class IntervalYearLogical implements LogicalType<Integer, I32Physical> {

    private IntervalYearLogical() {}

    public static final IntervalYearLogical INSTANCE = new IntervalYearLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public I32Physical getPhysicalType() {
        return I32Physical.INSTANCE;
    }
}
