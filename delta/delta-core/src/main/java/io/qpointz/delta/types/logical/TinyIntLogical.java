package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.I32Physical;
import io.qpointz.delta.types.sql.DatabaseType;
import io.qpointz.delta.vectors.VectorProducer;
import io.substrait.type.Type;
import io.substrait.type.TypeCreator;
import lombok.val;

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
