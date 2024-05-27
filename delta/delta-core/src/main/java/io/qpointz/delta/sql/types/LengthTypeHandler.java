package io.qpointz.delta.sql.types;

import io.substrait.proto.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
abstract class LengthTypeHandler implements TypeHandler {

    private final Type.Nullability nullability;

    private final int length;

}
