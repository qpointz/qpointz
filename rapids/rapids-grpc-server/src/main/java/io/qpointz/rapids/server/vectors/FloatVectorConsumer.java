package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatVectorConsumer extends VectorConsumer<Float> {

    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<Float> floats, Iterable<Boolean> nulls) {
        final var vector = FloatVector.newBuilder()
                .addAllValues(floats)
                .addAllNulls(nulls);
        vectorBuilder.setFloatVector(vector);
    }

    @Override
    protected Float getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        return resultSet.getFloat(columnIndex);
    }
}
