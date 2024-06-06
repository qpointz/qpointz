package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.Vector;
import io.substrait.proto.Type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BigIntTypeHandler extends SimpleTypeHandler<Long> {

    public BigIntTypeHandler(Type.Nullability nullability) {
        super(nullability);
    }

    @Override
    boolean hasVector(Vector vector) {
        return vector.hasI64Vector();
    }

    @Override
    Long readVectorValue(Vector vector, int rowIdx) {
        return vector.getI64Vector().getValues(rowIdx);
    }

    @Override
    public Type toSubstrait() {
        return Type.newBuilder().setI64(Type
                        .I64.newBuilder()
                        .setNullability(this.getNullability()))
                .build();
    }


    @Override
    public VectorProducer createVectorProducer() {
        return new LongVectorProducer();
    }

    static class LongVectorProducer implements VectorProducer {

        private final Vector.I64Vector.Builder builder = Vector.I64Vector.newBuilder();
        private final Vector.NullsVector.Builder nulls = Vector.NullsVector.newBuilder();

        @Override
        public void read(ResultSet rs, int idx) throws SQLException {
            builder.addValues(rs.getLong(idx));
            nulls.addNulls(rs.wasNull());
        }

        @Override
        public Vector toVector() {
            return Vector.newBuilder()
                    .setI64Vector(builder)
                    .setNulls(nulls)
                    .build();
        }
    }

}
