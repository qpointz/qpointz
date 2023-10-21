package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Int64VectorConsumer extends VectorConsumer<Long> {
    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<Long> longs, Iterable<Boolean> nulls) {
        final var vector = Int64Vector.newBuilder()
                .addAllValues(longs)
                .addAllNulls(nulls);
        vectorBuilder.setInt64Vector(vector);
    }

    @Override
    protected Long getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        return resultSet.getLong(columnIndex);
    }
}
