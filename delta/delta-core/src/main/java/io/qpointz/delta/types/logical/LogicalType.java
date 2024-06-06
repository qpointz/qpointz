package io.qpointz.delta.types.logical;

import io.qpointz.delta.types.physical.PhysicalType;
import io.qpointz.delta.vectors.VectorProducer;

public interface LogicalType<E, P extends PhysicalType<E>> {
    <T> T accept(LogicalTypeShuttle<T> shuttle);
    P getPhysicalType();

    default VectorProducer<E> getVectorProducer() {
        return this.getPhysicalType().createVectorProducer();
    }
}
