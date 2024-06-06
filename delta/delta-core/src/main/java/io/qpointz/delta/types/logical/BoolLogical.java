package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.BoolPhysical;
import io.qpointz.delta.types.physical.PhysicalType;

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
}
