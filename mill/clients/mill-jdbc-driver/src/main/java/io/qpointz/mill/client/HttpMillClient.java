package io.qpointz.mill.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.qpointz.mill.proto.*;
import lombok.*;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;
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

    @Getter
    private final String authenticationHeaderValue;

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
            return this.getRequestUrl() + path ;
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

            if (config.getProtocol()!=null && !config.getProtocol().isEmpty()) {
                this.protocol(config.getProtocol());
            }

            if (config.getBearerToken()!=null && !config.getBearerToken().isEmpty()) {
                this.useBearerToken(config.getBearerToken());
            } else if (config.getUsername()!=null && !config.getUsername().isEmpty() && config.getPassword()!=null && !config.getPassword().isEmpty()) {
                this.useBasicAuthentication(config.getUsername(), config.getPassword());
            } else {
                this.authenticationHeaderValue = null;
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

        public HttpMillClientBuilder useBasicAuthentication(String username, String password) {
            val headerValue = MillClientCallCredentials.basicAuthHeaderValue(username, password);
            return this.authenticationHeaderValue(headerValue);
        }

        public HttpMillClientBuilder useBearerToken(String bearerToken) {
            val headerValue = MillClientCallCredentials.bearerTokenHeaderValue(bearerToken);
            return this.authenticationHeaderValue(headerValue);
        }
    }

    private static final okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json");

    @SneakyThrows
    private <T extends Message> T post(String path, Message message, Function<byte[], T> produce) {
        val jsonMessage = JsonFormat.printer().print(message);
        val builder = new Request.Builder()
                .url(this.requestUrl(path))
                .post(RequestBody.create(jsonMessage.getBytes()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/protobuf");
        if (authenticationHeaderValue!=null) {
            builder.addHeader("Authorization", this.getAuthenticationHeaderValue());
        }
        val req = builder.build();
        val call = this.getHttpClient().newCall(req);
        try {
            val resp = call.execute();
            return produce.apply(resp.body().bytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getClientUrl() {
        return this.buildUrl();
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
        //no closable resources associated with client
    }

    @Override
    public Iterator<QueryResultResponse> execQuery(QueryRequest request) {
        return new QueryResultResponseIterator(request);
    }

    private class QueryResultResponseIterator implements Iterator<QueryResultResponse> {

        private final int fetchSize;
        private String pagingId;
        private QueryResultResponse response;
        private boolean didNext;
        private boolean hasNext;

        public QueryResultResponseIterator(QueryRequest initialRequest) {
            this.fetchSize = initialRequest.getConfig().getFetchSize();
            this.doInitial(initialRequest);
        }

        private void doInitial(QueryRequest initialRequest) {
            var initial = post("SubmitQuery", initialRequest, b-> {
                try {
                    return QueryResultResponse.parseFrom(b);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            });
            setNext(initial);
        }

        private void setNext(QueryResultResponse response) {
            this.didNext = true;
            this.response = response;
            this.pagingId = response!=null ? response.getPagingId() : null;
            this.hasNext = this.pagingId !=null && ! this.pagingId.isEmpty();
        }

        private void doNext() {
            if (!this.hasNext) {
                setNext(null);
                return;
            }

            val request = QueryResultRequest.newBuilder()
                    .setPagingId(this.pagingId)
                    .setFetchSize(this.fetchSize)
                    .build();

            val nextResponse = HttpMillClient.this.post("FetchQueryResult", request,b-> {
                try {
                    return QueryResultResponse.parseFrom(b);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            });

            setNext(nextResponse);
        }

        @Override
        public boolean hasNext() {
            if (!this.didNext) {
                doNext();
            }
            return this.hasNext;
        }

        @Override
        public QueryResultResponse next() {
            if (!this.didNext) {
                doNext();
            }
            if (this.response == null || !this.hasNext) {
                throw new NoSuchElementException("No results available");
            }
            this.didNext = false;
            return this.response;
        }
    }

}
