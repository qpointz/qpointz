package io.qpointz.mill.types.physical;

import io.qpointz.mill.vectors.VectorProducer;

public interface PhysicalType<B> {
    <T> T accept(PhysicalTypeShuttle<T> shuttle);
    VectorProducer<B> createVectorProducer();
}
