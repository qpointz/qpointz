package io.qpointz.mill.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.qpointz.mill.proto.*;
import lombok.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;

@AllArgsConstructor
@Builder
public class HttpMillClient extends MillClient implements AutoCloseable {


    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final OkHttpClient httpClient = createHttpClient();


    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    public static class HttpMillClientBuilder {

        public HttpMillClientBuilder() {
            this.apiPath("/api/");
            this.uriBuilder = new UriBuilder();
        }

        public HttpMillClientBuilder baseUrl(String value) {
            if (value !=null && !value.isEmpty()) {
                if (value.endsWith("/")) {
                    this.baseUrl = value.substring(0, value.length() - 1);
                } else {
                    this.baseUrl = value;
                }
                return this;
            }
            throw new IllegalArgumentException("baseUrl is invalid");
        }

        public HttpMillClientBuilder apiPath(String value) {
            if (value !=null && !value.isEmpty()) {
                if (! value.startsWith("/")) {
                    value = "/" + value;
                }

                if (! value.endsWith("/")) {
                    value += "/";
                }
                this.apiPath = value ;
                return this;
            }
            throw new IllegalArgumentException("apiPath is invalid");
        }

        public HttpMillClientBuilder useConfig(MillClientConfiguration config) {
            this.
        }

    }

    private String requestUrl(String s) {
        return baseUrl + apiPath + s;
    }

    private <T extends Message> T post(String path, Message message, Function<byte[], T> produce) {
        val req = new Request.Builder()
                .url(this.requestUrl(path))
                .post(RequestBody.create(message.toByteArray()))
                .build();

        val call = this.getHttpClient().newCall(req);
        try {
            val resp = call.execute();
            return produce.apply(resp.body().bytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public HandshakeResponse handshake(HandshakeRequest request) {
        return post("Handshake", request, b -> {
            try {
                return HandshakeResponse.parseFrom(b);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ListSchemasResponse listSchemas(ListSchemasRequest request) {
        return post("ListSchemas", request, b -> {
            try {
                return ListSchemasResponse.parseFrom(b);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public GetSchemaResponse getSchema(GetSchemaRequest request) {
        return post("GetSchema", request, b -> {
            try {
                return GetSchemaResponse.parseFrom(b);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public Iterator<QueryResultResponse> execQuery(QueryRequest request) {
        throw new UnsupportedOperationException();
    }
}
