package io.qpointz.rapids.server.vectors;

import com.google.protobuf.StringValue;
import io.qpointz.rapids.grpc.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringVectorConsumer extends VectorConsumer<String> {
    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<String> strings, Iterable<Boolean> nulls) {
        final var vector = StringVector.newBuilder()
                .addAllValues(strings)
                .addAllNulls(nulls);
        vectorBuilder.setStringVector(vector);
    }

    @Override
    protected String getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        return resultSet.getString(columnIndex);
    }

    @Override
    protected String nullValue() {
        return "";
    }
}
