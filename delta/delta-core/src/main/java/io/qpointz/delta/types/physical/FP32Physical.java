package io.qpointz.delta.types.physical;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.VectorProducerBase;

public final class FP32Physical implements PhysicalType<Float> { //fp32

    private FP32Physical() {}

    public static final FP32Physical INSTANCE = new FP32Physical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public VectorProducerBase<Float, Vector.FP32Vector.Builder> createVectorProducer() {
        return VectorProducerBase.createProducer(
                Vector.FP32Vector::newBuilder,
                0F,
                Vector.FP32Vector.Builder::addValues,
                Vector.Builder::setFp32Vector
        );
    }

}
