package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Int32VectorConsumer extends VectorConsumer<Integer> {
    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<Integer> integers, Iterable<Boolean> nulls) {
        final var vector = Int32Vector.newBuilder()
                .addAllValues(integers)
                .addAllNulls(nulls);
        vectorBuilder.setInt32Vector(vector);
    }

    @Override
    protected Integer getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        return resultSet.getInt(columnIndex);
    }

    @Override
    protected Integer nullValue() {
        return 0;
    }
}
