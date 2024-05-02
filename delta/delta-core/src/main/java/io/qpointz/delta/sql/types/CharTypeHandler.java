package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.Vector;
import io.substrait.proto.Type;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CharTypeHandler extends LengthTypeHandler {

    public CharTypeHandler(Type.Nullability nullability, int length) {
        super(nullability, length);
    }

    @Override
    public Type toSubstrait() {
        return Type.newBuilder().setFixedChar(Type
                        .FixedChar.newBuilder()
                        .setLength(this.getLength())
                        .setNullability(this.getNullability()))
                .build();
    }

    @Override
    public VectorProducer createVectorProducer() {
        return new StringVectorProducer();
    }

    static class StringVectorProducer implements VectorProducer {

        private final Vector.StringVector.Builder builder = Vector.StringVector.newBuilder();

        @Override
        public void read(ResultSet rs, int idx) throws SQLException {
            val str = rs.getString(idx);
            if (rs.wasNull()) {
                builder.addValues("");
                builder.addNulls(true);
            } else {
                builder.addValues(str);
                builder.addNulls(false);
            }
        }

        @Override
        public Vector toVector() {
            return Vector.newBuilder()
                    .setStringVector(builder.build())
                    .build();
        }
    }

}
