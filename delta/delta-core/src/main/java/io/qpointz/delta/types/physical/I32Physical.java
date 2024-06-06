package io.qpointz.delta.types.physical;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.PhysicalTypeVectorProducer;
import io.qpointz.delta.vectors.VectorProducer;

public final class I32Physical implements PhysicalType<Integer> {

    private I32Physical() {}

    public static final I32Physical INSTANCE = new I32Physical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public PhysicalTypeVectorProducer<Integer, Vector.I32Vector.Builder> createVectorProducer() {
        return PhysicalTypeVectorProducer.createProducer(
                Vector.I32Vector::newBuilder,
                0,
                Vector.I32Vector.Builder::addValues, Vector.I32Vector.Builder::addAllValues,
                Vector.Builder::setI32Vector
        );
    }
}
