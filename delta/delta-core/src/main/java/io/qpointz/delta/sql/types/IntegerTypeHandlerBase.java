package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.Vector;
import io.substrait.proto.Type;

import java.sql.ResultSet;
import java.sql.SQLException;

abstract class IntegerTypeHandlerBase extends SimpleTypeHandler<Integer> {

    protected IntegerTypeHandlerBase(Type.Nullability nullability) {
        super(nullability);
    }


    @Override
    public VectorProducer createVectorProducer() {
        return new IntegerVectorProducer();
    }

    @Override
    boolean hasVector(Vector vector) {
        return vector.hasInt32Vector();
    }

    @Override
    Integer readVectorValue(Vector vector, int rowIdx) {
        return vector.getInt32Vector().getValues(rowIdx);
    }

    @Override
    boolean isNullValue(Vector vector, int rowIdx) {
        return vector.getInt32Vector().getNulls(rowIdx);
    }

    static class IntegerVectorProducer implements VectorProducer {

        private final Vector.Int32Vector.Builder builder = Vector.Int32Vector.newBuilder();

        @Override
        public void read(ResultSet rs, int idx) throws SQLException {
            builder.addValues(rs.getInt(idx));
            builder.addNulls(rs.wasNull());
        }

        @Override
        public Vector toVector() {
            return Vector.newBuilder()
                    .setInt32Vector(builder.build())
                    .build();
        }
    }
}
