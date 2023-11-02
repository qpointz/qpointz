package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class BooleanVectorConsumer extends VectorConsumer<Boolean> {

    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<Boolean> values, Iterable<Boolean> nulls) {
        final var typeVector = BoolVector.newBuilder()
                .addAllValues(values)
                .addAllNulls(nulls);
        vectorBuilder.setBoolVector(typeVector);
    }

    @Override
    protected Boolean getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        return resultSet.getBoolean(columnIndex);
    }

    @Override
    protected Boolean nullValue() {
        return false;
    }
}

