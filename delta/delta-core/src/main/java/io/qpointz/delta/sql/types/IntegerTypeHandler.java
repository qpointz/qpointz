package io.qpointz.delta.sql.types;

import io.substrait.proto.Type;

public class IntegerTypeHandler extends  IntegerTypeHandlerBase {

    public IntegerTypeHandler(Type.Nullability nullability) {
        super(nullability);
    }

    @Override
    public Type toSubstrait() {
        return Type.newBuilder().setI32(Type
                        .I32.newBuilder()
                        .setNullability(this.getNullability()))
                .build();
    }

}
