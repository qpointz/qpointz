package io.qpointz.delta.sql.types;

import io.substrait.proto.Type;

public class TinyIntTypeHandler extends IntegerTypeHandlerBase {

    public TinyIntTypeHandler(Type.Nullability nullability) {
        super(nullability);
    }

    @Override
    public Type toSubstrait() {
        return Type.newBuilder().setI8(Type
                    .I8.newBuilder()
                    .setNullability(getNullability()))
                .build();
    }

}
