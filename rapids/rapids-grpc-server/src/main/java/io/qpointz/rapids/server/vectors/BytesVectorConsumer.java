package io.qpointz.rapids.server.vectors;

import com.google.protobuf.ByteString;
import io.qpointz.rapids.grpc.ByteVector;
import io.qpointz.rapids.grpc.Vector;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.StreamSupport;

public class BytesVectorConsumer extends VectorConsumer<ByteString> {

    @Override
    protected void vector(Vector.Builder vectorBuilder, Iterable<ByteString> bytes, Iterable<Boolean> nulls) {
        final var vector = ByteVector.newBuilder()
                .addAllValues(bytes)
                .addAllNulls(nulls);
        vectorBuilder.setByteVector(vector);
    }

    @Override
    protected ByteString getValue(ResultSet resultSet, int columnIndex) throws SQLException {
        final var blob = resultSet.getBytes(columnIndex);
        if (blob==null || blob.length ==0) {
            return ByteString.empty();
        }
        return ByteString.copyFrom(blob);
//        return ByteString.empty();
//        try {
//            var bytestring = ByteString.readFrom(blob.getBinaryStream());
//            return bytestring;
//        } catch (IOException e) {
//            throw new SQLException("Failed to read binary data", e);
//        }

    }

    @Override
    protected ByteString nullValue() {
        return ByteString.empty();
    }
}
