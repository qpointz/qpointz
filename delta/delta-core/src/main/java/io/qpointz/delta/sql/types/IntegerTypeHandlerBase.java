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
        return vector.hasI32Vector();
    }

    @Override
    Integer readVectorValue(Vector vector, int rowIdx) {
        return vector.getI32Vector().getValues(rowIdx);
    }

    static class IntegerVectorProducer implements VectorProducer {

        private final Vector.I32Vector.Builder builder = Vector.I32Vector.newBuilder();
        private final Vector.NullsVector.Builder nulls = Vector.NullsVector.newBuilder();

        @Override
        public void read(ResultSet rs, int idx) throws SQLException {
            builder.addValues(rs.getInt(idx));
            nulls.addNulls(rs.wasNull());
        }

        @Override
        public Vector toVector() {
            return Vector.newBuilder()
                    .setI32Vector(builder)
                    .setNulls(nulls)
                    .build();
        }
    }
}
