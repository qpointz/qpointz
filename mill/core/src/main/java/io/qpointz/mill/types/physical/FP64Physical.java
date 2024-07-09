package io.qpointz.mill.types.physical;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.vectors.VectorProducerBase;

public final class FP64Physical implements PhysicalType<Double> { //fp64

    private FP64Physical() {}

    public static final FP64Physical INSTANCE = new FP64Physical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public VectorProducerBase<Double, Vector.FP64Vector.Builder> createVectorProducer() {
        return VectorProducerBase.createProducer(
                Vector.FP64Vector::newBuilder,
                0D,
                Vector.FP64Vector.Builder::addValues,
                Vector.Builder::setFp64Vector
        );
    }

}
