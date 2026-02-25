package io.qpointz.mill.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.proto.*;
import lombok.*;
import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@AllArgsConstructor
@Builder
public class HttpMillClient extends MillClient {

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
    private static final okhttp3.MediaType PROTOBUF = okhttp3.MediaType.get("application/x-protobuf");

    private <T extends Message> T post(String path, Message message, Function<byte[], T> produce) throws MillCodeException {
        String jsonMessage = null;
        try {
            jsonMessage = JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new MillCodeException(e);
        }
        val builder = new Request.Builder()
                .url(this.requestUrl(path))
                .post(RequestBody.create(jsonMessage.getBytes()))
                .addHeader("Content-Type", JSON.toString())
                .addHeader("Accept", PROTOBUF.toString());
        if (authenticationHeaderValue!=null) {
            builder.addHeader("Authorization", this.getAuthenticationHeaderValue());
        }
        val req = builder.build();
        val call = this.getHttpClient().newCall(req);

        Response resp;
        try {
            resp = call.execute();
        } catch (IOException e) {
            throw new MillCodeException("Failed to execute HTTP request", e);
        }

        if (!resp.isSuccessful()) {
            throw new MillCodeException("HTTP request failed with status code: " + resp.code() + " and message: " + resp.message());
        }

        try {
            return produce.apply(resp.body().bytes());
        } catch (IOException e) {
            throw new MillCodeException("Failed to read response body", e);
        }
    }

    private <T extends Message> CompletableFuture<T> postAsync(String path, Message message, Function<byte[], T> produce) {
        final String jsonMessage;
        try {
            jsonMessage = JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            return CompletableFuture.failedFuture(new MillCodeException(e));
        }

        val builder = new Request.Builder()
                .url(this.requestUrl(path))
                .post(RequestBody.create(jsonMessage.getBytes()))
                .addHeader("Content-Type", JSON.toString())
                .addHeader("Accept", PROTOBUF.toString());
        if (authenticationHeaderValue != null) {
            builder.addHeader("Authorization", this.getAuthenticationHeaderValue());
        }
        val req = builder.build();
        val call = this.getHttpClient().newCall(req);
        val result = new CompletableFuture<T>();

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result.completeExceptionally(new MillCodeException("Failed to execute HTTP request", e));
            }

            @Override
            public void onResponse(Call call, Response resp) {
                try (resp) {
                    if (!resp.isSuccessful()) {
                        result.completeExceptionally(new MillCodeException(
                                "HTTP request failed with status code: " + resp.code() + " and message: " + resp.message()));
                        return;
                    }

                    result.complete(produce.apply(resp.body().bytes()));
                } catch (Exception e) {
                    result.completeExceptionally(new MillCodeException("Failed to read response body", e));
                }
            }
        });

        return result;
    }

    @Override
    public String getClientUrl() {
        return this.buildUrl();
    }

    @Override
    public HandshakeResponse handshake(HandshakeRequest request) throws MillCodeException {
        return post("Handshake", request, b -> {
            try {
                return HandshakeResponse.parseFrom(b);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ListSchemasResponse listSchemas(ListSchemasRequest request) throws MillCodeException {
        return post("ListSchemas", request, b -> {
            try {
                return ListSchemasResponse.parseFrom(b);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public GetSchemaResponse getSchema(GetSchemaRequest request) throws MillCodeException {
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
    public MillQueryResult execQuery(QueryRequest request) {
        return MillQueryResult.fromResponses(new QueryResultResponseIterator(request));
    }

    @Override
    public CompletableFuture<MillQueryResult> execQueryAsync(QueryRequest request) {
        return postAsync("SubmitQuery", request, b -> {
            try {
                return QueryResultResponse.parseFrom(b);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }).thenApply(initial -> MillQueryResult.fromResponses(new QueryResultResponseIterator(request.getConfig().getFetchSize(), initial)));
    }

    private class QueryResultResponseIterator implements Iterator<QueryResultResponse> {

        private final int fetchSize;
        private String pagingId;
        private QueryResultResponse response;
        private boolean didNext;
        private boolean hasNext;
        private boolean hasMorePages;

        public QueryResultResponseIterator(QueryRequest initialRequest) {
            this.fetchSize = initialRequest.getConfig().getFetchSize();
            setNext(doInitial(initialRequest));
        }

        private QueryResultResponseIterator(int fetchSize, QueryResultResponse initial) {
            this.fetchSize = fetchSize;
            setNext(initial);
        }

        private QueryResultResponse doInitial(QueryRequest initialRequest) {
            QueryResultResponse initial = null;
            try {
                initial = HttpMillClient.this.post("SubmitQuery", initialRequest, b -> {
                    try {
                        return QueryResultResponse.parseFrom(b);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (MillCodeException e) {
                throw new MillRuntimeException(e);
            }
            return initial;
        }

        private void setNext(QueryResultResponse response) {
            this.didNext = true;
            this.response = response;
            this.pagingId = response!=null ? response.getPagingId() : null;
            this.hasNext = response != null;
            this.hasMorePages = this.pagingId !=null && !this.pagingId.isEmpty();
        }

        private void doNext() {
            if (!this.hasMorePages) {
                setNext(null);
                return;
            }

            val request = QueryResultRequest.newBuilder()
                    .setPagingId(this.pagingId)
                    .setFetchSize(this.fetchSize)
                    .build();

            try {
                val nextResponse = HttpMillClient.this.post("FetchQueryResult", request, b -> {
                    try {
                        return QueryResultResponse.parseFrom(b);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                });
                setNext(nextResponse);
            } catch (MillCodeException ex1) {
                throw new MillRuntimeException(ex1);
            }
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
            if (this.response == null) {
                throw new NoSuchElementException("No results available");
            }
            this.didNext = false;
            return this.response;
        }
    }

}
