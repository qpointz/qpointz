package io.qpointz.mill.types.logical;

import io.qpointz.mill.types.physical.StringPhysical;

public final class StringLogical implements LogicalType<String, StringPhysical> {

    private StringLogical() {
    }

    public static final StringLogical INSTANCE = new StringLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public StringPhysical getPhysicalType() {
        return StringPhysical.INSTANCE;
    }
}
