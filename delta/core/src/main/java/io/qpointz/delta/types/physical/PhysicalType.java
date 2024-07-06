package io.qpointz.delta.types.physical;

import io.qpointz.delta.vectors.VectorProducer;

public interface PhysicalType<B> {
    <T> T accept(PhysicalTypeShuttle<T> shuttle);
    VectorProducer<B> createVectorProducer();
}
