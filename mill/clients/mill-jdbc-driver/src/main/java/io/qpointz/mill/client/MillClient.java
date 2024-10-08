package io.qpointz.mill.client;

import io.qpointz.mill.proto.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.val;

import java.util.Iterator;

import static io.qpointz.mill.client.MillClientConfiguration.*;

@Log
public abstract class MillClient implements AutoCloseable {

    public static MillClient fromConfig(MillClientConfiguration millClientConfiguration) {
        val proto = millClientConfiguration.getProtocol();
        if (proto.equals(CLIENT_PROTOCOL_IN_PROC_VALUE) || proto.equals(CLIENT_PROTOCOL_GRPC_VALUE)) {
            return new GrpcMillClient(millClientConfiguration);
        }

        if (proto.equals(CLIENT_PROTOCOL_HTTP_VALUE) || proto.equals(CLIENT_PROTOCOL_HTTPS_VALUE)) {
            return HttpMillClient.builder()
                    .useConfig(millClientConfiguration)
                    .build();
        }

        throw new IllegalArgumentException("Unsupported protocol: " + proto);
    }

    public abstract HandshakeResponse handshake(HandshakeRequest request);

    public abstract ListSchemasResponse listSchemas(ListSchemasRequest request);

    public abstract GetSchemaResponse getSchema(GetSchemaRequest request);

    public abstract Iterator<QueryResultResponse> execQuery(QueryRequest request);
}
