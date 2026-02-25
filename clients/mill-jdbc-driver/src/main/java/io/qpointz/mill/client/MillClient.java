package io.qpointz.mill.client;

import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.proto.*;
import lombok.extern.java.Log;
import lombok.val;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

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

    public abstract String getClientUrl();

    public abstract HandshakeResponse handshake(HandshakeRequest request) throws MillCodeException;

    public abstract ListSchemasResponse listSchemas(ListSchemasRequest request) throws MillCodeException;

    public abstract GetSchemaResponse getSchema(GetSchemaRequest request) throws MillCodeException;

    public abstract MillQueryResult execQuery(QueryRequest request) throws MillCodeException;

    protected Executor asyncExecutor() {
        return ForkJoinPool.commonPool();
    }

    public CompletableFuture<HandshakeResponse> handshakeAsync(HandshakeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return handshake(request);
            } catch (MillCodeException e) {
                throw new CompletionException(e);
            }
        }, asyncExecutor());
    }

    public CompletableFuture<ListSchemasResponse> listSchemasAsync(ListSchemasRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return listSchemas(request);
            } catch (MillCodeException e) {
                throw new CompletionException(e);
            }
        }, asyncExecutor());
    }

    public CompletableFuture<GetSchemaResponse> getSchemaAsync(GetSchemaRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getSchema(request);
            } catch (MillCodeException e) {
                throw new CompletionException(e);
            }
        }, asyncExecutor());
    }

    public CompletableFuture<MillQueryResult> execQueryAsync(QueryRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execQuery(request);
            } catch (MillCodeException e) {
                throw new CompletionException(e);
            }
        }, asyncExecutor());
    }
}
