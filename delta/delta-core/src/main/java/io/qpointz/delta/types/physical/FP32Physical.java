package io.qpointz.delta.types.physical;

import com.google.protobuf.ByteString;
import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.PhysicalTypeVectorProducer;

public final class FP32Physical implements PhysicalType<Float> { //fp32

    private FP32Physical() {}

    public static final FP32Physical INSTANCE = new FP32Physical();

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public PhysicalTypeVectorProducer<Float, Vector.FP32Vector.Builder> createVectorProducer() {
        return PhysicalTypeVectorProducer.createProducer(
                Vector.FP32Vector::newBuilder,
                0F,
                Vector.FP32Vector.Builder::addValues, Vector.FP32Vector.Builder::addAllValues,
                Vector.Builder::setFp32Vector
        );
    }

}
