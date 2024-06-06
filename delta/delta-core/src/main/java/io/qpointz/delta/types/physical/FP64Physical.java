package io.qpointz.delta.types.physical;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.PhysicalTypeVectorProducer;

public final class FP64Physical implements PhysicalType<Double> { //fp64

    private FP64Physical() {}

    public static final FP64Physical INSTANCE = new FP64Physical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public PhysicalTypeVectorProducer<Double, Vector.FP64Vector.Builder> createVectorProducer() {
        return PhysicalTypeVectorProducer.createProducer(
                Vector.FP64Vector::newBuilder,
                0D,
                Vector.FP64Vector.Builder::addValues, Vector.FP64Vector.Builder::addAllValues,
                Vector.Builder::setFp64Vector
        );
    }

}
