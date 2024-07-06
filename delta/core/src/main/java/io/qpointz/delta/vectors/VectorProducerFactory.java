package io.qpointz.delta.vectors;

import io.qpointz.delta.types.physical.*;

public class VectorProducerFactory implements PhysicalTypeShuttle<VectorProducer<?>> {

    public static VectorProducerFactory DEFAULT = new VectorProducerFactory();

    @Override
    public VectorProducer<byte[]> visit(BytesPhysical binaryType) {
        return binaryType.createVectorProducer();
    }

    @Override
    public VectorProducer<Boolean> visit(BoolPhysical boolType) {
        return boolType.createVectorProducer();
    }

    @Override
    public VectorProducer<Float> visit(FP32Physical fp32Type) {
        return fp32Type.createVectorProducer();
    }

    @Override
    public VectorProducer<Double> visit(FP64Physical fp64Type) {
        return fp64Type.createVectorProducer();
    }

    @Override
    public VectorProducer<Integer> visit(I32Physical i32Type) {
        return i32Type.createVectorProducer();
    }

    @Override
    public VectorProducer<Long> visit(I64Physical i64Type) {
        return i64Type.createVectorProducer();
    }

    @Override
    public VectorProducer<String> visit(StringPhysical stringType) {
        return stringType.createVectorProducer();
    }
}
