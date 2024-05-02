package io.qpointz.delta.sql.types;

import io.substrait.proto.Type;

public class VarCharTypeHandler extends LengthTypeHandler {

    public VarCharTypeHandler(Type.Nullability nullability, int length) {
        super(nullability, length);
    }

    @Override
    public Type toSubstrait() {
        return Type.newBuilder().setVarchar(Type
                    .VarChar.newBuilder()
                    .setLength(this.getLength())
                    .setNullability(this.getNullability()))
                .build();
    }

    @Override
    public VectorProducer createVectorProducer() {
        return new CharTypeHandler.StringVectorProducer();
    }
}
