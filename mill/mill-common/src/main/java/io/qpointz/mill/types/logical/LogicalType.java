package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.physical.PhysicalType;
import io.qpointz.mill.vectors.VectorProducer;

public interface LogicalType<E, P extends PhysicalType<E>> {
    <T> T accept(LogicalTypeShuttle<T> shuttle);
    P getPhysicalType();
    LogicalDataType.LogicalDataTypeId getLogicalTypeId();

    default VectorProducer<E> getVectorProducer() {
        return this.getPhysicalType().createVectorProducer();
    }

}
