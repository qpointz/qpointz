package io.qpointz.delta.sql.types;

import io.substrait.proto.Type;

public interface TypeHandler {
    Type toSubstrait();
    Type.Nullability getNullability();
    VectorProducer createVectorProducer();
}
