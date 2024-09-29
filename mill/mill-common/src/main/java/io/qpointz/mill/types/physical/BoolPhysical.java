package io.qpointz.mill.types.physical;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.vectors.VectorProducerBase;

public final class BoolPhysical implements PhysicalType<Boolean> {

    private BoolPhysical() {}

    public static final BoolPhysical INSTANCE = new BoolPhysical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public VectorProducerBase<Boolean, Vector.BoolVector.Builder> createVectorProducer() {
        return VectorProducerBase.createProducer(
                Vector.BoolVector::newBuilder,
                false,
                Vector.BoolVector.Builder::addValues,
                Vector.Builder::setBoolVector
        );
    }

}
