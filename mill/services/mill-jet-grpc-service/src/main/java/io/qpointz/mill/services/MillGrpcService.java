package io.qpointz.mill.services;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.vectors.VectorBlockIterator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@GrpcService
@SpringBootApplication
@ConditionalOnService("grpc")
public class MillGrpcService extends DataConnectServiceGrpc.DataConnectServiceImplBase {

    @Bean
    public static SecurityProvider securityProvider() {
        return new SecurityContextSecurityProvider();
    }

    public MillGrpcService(@Autowired ServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
        this.dataOpDispatcher = serviceHandler.data();
    }

    @Getter(AccessLevel.PROTECTED)
    private final ServiceHandler serviceHandler;

    @Getter(AccessLevel.PROTECTED)
    private final DataOperationDispatcher dataOpDispatcher;

    protected <R,S> void replyOne(R request, StreamObserver<S> observer, Function<R, S> producer) {
        observer.onNext(producer.apply(request));
        observer.onCompleted();
    }

    private void traceRequest(String name, Supplier<String> toString) {
        if (log.isTraceEnabled()) {
            log.trace("{} request:{}", name, toString.get());
        }
    }

    @Override
    public void handshake(HandshakeRequest request, StreamObserver<HandshakeResponse> observer) {
        traceRequest("Handshake", request::toString);
        replyOne(request, observer, r-> this.dataOpDispatcher.handshake(request));
    }

    @Override
    public void listSchemas(ListSchemasRequest request, StreamObserver<ListSchemasResponse> responseObserver) {
        traceRequest("listSchemas", request::toString);
        replyOne(request, responseObserver, r-> this.dataOpDispatcher.listSchemas(request));
    }

    @Override
    public void getSchema(GetSchemaRequest request, StreamObserver<GetSchemaResponse> responseObserver) {
        traceRequest("getSchema", request::toString);
        replyOne(request, responseObserver, r-> this.dataOpDispatcher.getSchema(request));
    }

    @Override
    public void parseSql(ParseSqlRequest request, StreamObserver<ParseSqlResponse> responseObserver) {
        traceRequest("parseSql", request::toString);
        replyOne(request, responseObserver, r-> this.dataOpDispatcher.parseSql(request));
    }

    @Override
    public void execQuery(QueryRequest request, StreamObserver<QueryResultResponse> responseObserver) {
        traceRequest("execQuery", request::toString);
        val iterator = this.dataOpDispatcher.execute(request);
        streamResult(iterator, responseObserver);
    }

    protected static void streamResult(VectorBlockIterator iterator, StreamObserver<QueryResultResponse> responseObserver) {
        var callObserver = (ServerCallStreamObserver<QueryResultResponse>)responseObserver;
        while (iterator.hasNext()) {
            val vectorBlock = iterator.next();
            val resp = QueryResultResponse.newBuilder()
                    .setVector(vectorBlock)
                    .build();
            callObserver.onNext(resp);
        }
        callObserver.onCompleted();
    }

}