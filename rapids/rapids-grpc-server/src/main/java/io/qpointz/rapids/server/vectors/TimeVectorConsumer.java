package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.Int64Vector;
import io.qpointz.rapids.grpc.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeVectorConsumer extends VectorConsumer<Long> {
    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<Long> longs, Iterable<Boolean> nulls) {
        final var vector = Int64Vector.newBuilder()
                .addAllValues(longs)
                .addAllNulls(nulls);
        vectorBuilder.setInt64Vector(vector);
    }

    @Override
    protected Long getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        final var value = resultSet.getTime(columnIndex);
        if (value==null) {
            return this.nullValue();
        }
        return value.toLocalTime().toNanoOfDay();
    }

    @Override
    protected Long nullValue() {
        return 0L;
    }
}
