package io.qpointz.delta.types.physical;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.VectorProducerBase;

public final class I64Physical implements PhysicalType<Long> {

    public I64Physical() {}

    public static I64Physical INSTANCE = new I64Physical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public VectorProducerBase<Long, Vector.I64Vector.Builder> createVectorProducer() {
        return VectorProducerBase.createProducer(
                Vector.I64Vector::newBuilder,
                0L,
                Vector.I64Vector.Builder::addValues,
                Vector.Builder::setI64Vector
        );
    }

}
