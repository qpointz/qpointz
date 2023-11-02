package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleVectorConsumer extends VectorConsumer<Double> {

    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<Double> doubles, Iterable<Boolean> nulls) {
        final var vector = DoubleVector.newBuilder()
                .addAllValues(doubles)
                .addAllNulls(nulls);
        vectorBuilder.setDoubleVector(vector);
    }

    @Override
    protected Double getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        return resultSet.getDouble(columnIndex);
    }

    @Override
    protected Double nullValue() {
        return 0D;
    }
}
