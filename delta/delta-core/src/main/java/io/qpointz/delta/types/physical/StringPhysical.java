package io.qpointz.delta.types.physical;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.PhysicalTypeVectorProducer;

public final class StringPhysical implements PhysicalType<String> {

    private StringPhysical() {
    }

    public static final StringPhysical INSTANCE = new StringPhysical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public PhysicalTypeVectorProducer<String, Vector.StringVector.Builder> createVectorProducer() {
        return PhysicalTypeVectorProducer.createProducer(
                Vector.StringVector::newBuilder,
                "",
                Vector.StringVector.Builder::addValues, Vector.StringVector.Builder::addAllValues,
                Vector.Builder::setStringVector
        );
    }

}
