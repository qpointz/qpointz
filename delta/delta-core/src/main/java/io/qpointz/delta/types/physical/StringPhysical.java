package io.qpointz.delta.types.physical;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.VectorProducerBase;

public final class StringPhysical implements PhysicalType<String> {

    private StringPhysical() {
    }

    public static final StringPhysical INSTANCE = new StringPhysical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public VectorProducerBase<String, Vector.StringVector.Builder> createVectorProducer() {
        return VectorProducerBase.createProducer(
                Vector.StringVector::newBuilder,
                "",
                Vector.StringVector.Builder::addValues,
                Vector.Builder::setStringVector
        );
    }

}
