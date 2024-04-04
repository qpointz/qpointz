package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.Vector;
import io.substrait.proto.Type;
import lombok.AllArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTypeHandler extends SimpleTypeHandler<Boolean> {

    private BooleanTypeHandler(Type.Nullability nullability) {
        super(nullability);
    }

    public static BooleanTypeHandler get(Type.Nullability nullability) {
        return new BooleanTypeHandler(nullability);
    }

    @Override
    public Type toSubstrait() {
        return Type.newBuilder().setBool(Type
                .Boolean.newBuilder()
                .setNullability(this.getNullability()))
                .build();
    }

    @Override
    public VectorProducer createVectorProducer() {
        return new VectorBlockProducer();
    }

    @Override
    boolean hasVector(Vector vector) {
        return vector.hasBoolVector();
    }

    @Override
    Boolean readVectorValue(Vector vector, int rowIdx) {
        return vector.getBoolVector().getValues(rowIdx);
    }

    @Override
    boolean isNullValue(Vector vector, int rowIdx) {
        return vector.getBoolVector().getNulls(rowIdx);
    }

    private class VectorBlockProducer implements VectorProducer {

        private final Vector.BoolVector.Builder builder = Vector.BoolVector.newBuilder();

        @Override
        public void read(ResultSet rs, int idx) throws SQLException {
            builder.addValues(rs.getBoolean(idx));
            builder.addNulls(rs.wasNull());
        }

        @Override
        public Vector toVector() {
            return Vector.newBuilder()
                    .setBoolVector(builder.build())
            .build();
        }
    }

}
