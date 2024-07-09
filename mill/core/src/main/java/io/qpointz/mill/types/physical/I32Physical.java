package io.qpointz.mill.types.physical;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.vectors.VectorProducerBase;

public final class I32Physical implements PhysicalType<Integer> {

    private I32Physical() {}

    public static final I32Physical INSTANCE = new I32Physical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public VectorProducerBase<Integer, Vector.I32Vector.Builder> createVectorProducer() {
        return VectorProducerBase.createProducer(
                Vector.I32Vector::newBuilder,
                0,
                Vector.I32Vector.Builder::addValues,
                Vector.Builder::setI32Vector
        );
    }
}
