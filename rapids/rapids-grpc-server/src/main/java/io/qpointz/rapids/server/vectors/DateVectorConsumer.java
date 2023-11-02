package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.Int64Vector;
import io.qpointz.rapids.grpc.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateVectorConsumer extends VectorConsumer<Long> {
    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<Long> longs, Iterable<Boolean> nulls) {
        final var vector = Int64Vector.newBuilder()
                .addAllValues(longs)
                .addAllNulls(nulls);
        vectorBuilder.setInt64Vector(vector);
    }

    @Override
    protected Long getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        final var value = resultSet.getDate(columnIndex);
        if (value==null || resultSet.wasNull()) {
            return this.nullValue();
        }
        return ChronoUnit.DAYS.between(Instant.EPOCH, value.toInstant());
    }

    @Override
    protected Long nullValue() {
        return LocalDate.MIN.toEpochDay();
    }
}
