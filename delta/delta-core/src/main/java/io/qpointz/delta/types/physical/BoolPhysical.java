package io.qpointz.delta.types.physical;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.PhysicalTypeVectorProducer;

public final class BoolPhysical implements PhysicalType<Boolean> {

    private BoolPhysical() {}

    public static final BoolPhysical INSTANCE = new BoolPhysical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public PhysicalTypeVectorProducer<Boolean, Vector.BoolVector.Builder> createVectorProducer() {
        return PhysicalTypeVectorProducer.createProducer(
                Vector.BoolVector::newBuilder,
                false,
                Vector.BoolVector.Builder::addValues, Vector.BoolVector.Builder::addAllValues,
                Vector.Builder::setBoolVector
        );
    }

}
