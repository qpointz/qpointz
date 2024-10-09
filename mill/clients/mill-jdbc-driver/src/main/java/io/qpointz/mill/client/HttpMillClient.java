package io.qpointz.mill.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.qpointz.mill.proto.*;
import lombok.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.function.Function;

@AllArgsConstructor
@Builder
public class HttpMillClient extends MillClient implements AutoCloseable {

    @Getter
    private final String protocol;

    @Getter
    private final String host;

    @Getter
    private final int port;

    @Getter
    private final String path;

    @Getter(lazy = true)
    private final String requestUrl = buildUrl();

    private String buildUrl() {
        val sb = new StringBuilder();
        sb.append(protocol);
        sb.append("://");
        sb.append(host);
        if (this.port>0) {
            sb.append(":");
            sb.append(port);
        }

        if (!this.path.startsWith("/")) {
            sb.append("/");
        }
        sb.append(this.path);
        if (!this.path.endsWith("/")) {
            sb.append("/");
        }
        return sb.toString();
    }

    private String requestUrl(String path) {
        if (path.startsWith("/")) {
            return this.getRequestUrl() + path.substring(1);
        } else {
            return this.getRequestUrl() + path;
        }
    }

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final OkHttpClient httpClient = createHttpClient();


    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    public static class HttpMillClientBuilder {

        public HttpMillClientBuilder useConfig(MillClientConfiguration config) {
            if (config.getPort()>0) {
                this.port(config.getPort());
            }
            if (config.getHost()!=null && !config.getHost().isEmpty()) {
                this.host(config.getHost());
            }
            if (config.getPath()!=null && !config.getPath().isEmpty()) {
                this.path(config.getPath());
            }

            return this;
        }

        public HttpMillClientBuilder url(String url) {
            val uri = URI.create(url);
            return this
                    .protocol(uri.getScheme())
                    .host(uri.getHost())
                    .port(uri.getPort())
                    .path(uri.getPath());
        }
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
