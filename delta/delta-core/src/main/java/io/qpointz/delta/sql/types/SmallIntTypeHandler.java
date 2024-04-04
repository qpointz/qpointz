package io.qpointz.delta.sql.types;

import io.substrait.proto.Type;

public class SmallIntTypeHandler extends IntegerTypeHandlerBase {

    public SmallIntTypeHandler(Type.Nullability nullability) {
        super(nullability);
    }

    @Override
    public Type toSubstrait() {
        return Type.newBuilder().setI16(Type
                    .I16.newBuilder()
                    .setNullability(this.getNullability()))
                .build();
    }
}
