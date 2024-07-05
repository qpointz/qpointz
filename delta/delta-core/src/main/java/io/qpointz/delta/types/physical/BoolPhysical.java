package io.qpointz.delta.types.physical;

import com.google.protobuf.Message;
import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.VectorProducerBase;

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
