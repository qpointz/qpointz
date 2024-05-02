package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.Vector;
import io.substrait.proto.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
abstract class SimpleTypeHandler<T> implements TypeHandler, VectorReader<T> {

    private final Type.Nullability nullability;

    @Override
    public T read(Vector vector, int rowIdx) {
        if (!this.hasVector(vector)) {
            throw new IllegalArgumentException("Vector type missmatch");
        }
        return readVectorValue(vector, rowIdx);
    }

    @Override
    public boolean isNull(Vector vector, int rowIdx) {
        if (!this.hasVector(vector)) {
            throw new IllegalArgumentException("Vector type missmatch");
        }
        return isNullValue(vector, rowIdx);
    }

    abstract boolean hasVector(Vector vector);

    abstract T readVectorValue(Vector vector, int rowIdx);

    abstract boolean isNullValue(Vector vector, int rowIdx);

}
