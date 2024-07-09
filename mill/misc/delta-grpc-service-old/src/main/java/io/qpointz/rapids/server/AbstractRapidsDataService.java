package io.qpointz.rapids.server;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.qpointz.rapids.grpc.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.function.Function;

@Slf4j
public abstract class AbstractRapidsDataService extends RapidsDataServiceGrpc.RapidsDataServiceImplBase {

    @Override
    public void handshake(HandshakeRequest request, StreamObserver<HandshakeResponse> responseObserver) {
        process(request, responseObserver, this::onHandshakeRequest);
    }

    @Override
    public void getCatalog(GetCatalogRequest request, StreamObserver<GetCatalogResponse> responseObserver) {
        process(request, responseObserver, this::onGetCatalog);
    }

    @Override
    public void listCatalogs(ListCatalogRequest request, StreamObserver<ListCatalogResponse> responseObserver) {
        process(request,responseObserver, this::onListCatalogs);
    }

    protected <R, P> void process(R request, StreamObserver<P> response, Function<R, P> onHandle) {
        try {
            final var reply = onHandle.apply(request);
            if (reply!=null) {
                response.onNext(reply);
                response.onCompleted();
            } else {
                response.onError(new NullPointerException("Wrong or Empty Response"));
            }
        } catch (Exception ex) {
            log.error("Server error", ex);
            response.onError(ex);
        }
    }

    protected abstract ListCatalogResponse onListCatalogs(ListCatalogRequest listCatalogRequest);

    protected HandshakeResponse onHandshakeRequest(HandshakeRequest handshakeRequest) {
        return defaultHandshake;
    }

    protected static ServiceVersion currentVersion = ServiceVersion.V_10;

    protected static HandshakeResponse defaultHandshake = HandshakeResponse.newBuilder()
            .setStatus(ResponseStatuses.statusOk())
            .setVersion(currentVersion)
            .build();

    protected abstract GetCatalogResponse onGetCatalog(GetCatalogRequest getCatalogRequest) ;

    @Builder
    @AllArgsConstructor
    public static class ExecQueryStreamResult {

        @Getter
        private ResponseStatus status;

        @Getter
        private Schema schema;

        @Getter
        Iterator<VectorBlock> vectorBlocks;
    }

    @Override
    public void execQueryStream(ExecQueryStreamRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        ServerCallStreamObserver<ExecQueryResponse> serverCallObserver = null;
        try {
            serverCallObserver = (ServerCallStreamObserver<ExecQueryResponse>)responseObserver;
        } catch (Exception e) {
            log.warn("ServerCall observer not available. Backpressure/cancelation will be ignored");
        }
        try {
            var result = onExecQueryStream(request);

            if (result.status.getCode() != ResponseCode.OK) {

                responseObserver.onNext(ExecQueryResponse.newBuilder().setStatus(result.status).build());
                responseObserver.onCompleted();
                return;
            }

            var schemaResponse = ExecQueryResponse.newBuilder()
                    .setSchema(result.getSchema());

            if (result.schema==null) {
                schemaResponse
                    .setStatus(ResponseStatuses.statusError("Schema unknown"));
                responseObserver.onNext(schemaResponse.build());
                responseObserver.onCompleted();
                return;
            }

            schemaResponse.setStatus(ResponseStatuses.statusOk());
            responseObserver.onNext(schemaResponse.build());

            if (result.vectorBlocks==null) {
                responseObserver.onCompleted();
                return;
            }

            var vectorBlock = result.vectorBlocks.next();
            var canceled = serverCallObserver!=null && serverCallObserver.isCancelled();
            var ready = true;

            while (vectorBlock!=null) {
                if (canceled) {
                    log.info("Request canceled. Exiting");
                    responseObserver.omCompleted();
                    break;
                }

                if (serverCallObserver!=null) {
                    ready = serverCallObserver.isReady();
                }

                if (!ready) {
                    log.info("Consumer not ready sleeping for {} ms", 200);
                    Thread.sleep(200);
                    continue;
                }

                final var blockResponse = ExecQueryResponse.newBuilder()
                        .setStatus(ResponseStatuses.statusOk())
                        .setVector(vectorBlock)
                        .build();
                responseObserver.onNext(blockResponse);

                vectorBlock = result.vectorBlocks.next();
                log.info("next block");
                canceled = serverCallObserver!=null && serverCallObserver.isCancelled();
            }
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(ex);
        }
    }

    protected abstract ExecQueryStreamResult onExecQueryStream(ExecQueryStreamRequest request);


    @Override
    public void execQuery(ExecQueryRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        process(request, responseObserver, this::onExecQuery);
    }

    protected abstract ExecQueryResponse onExecQuery(ExecQueryRequest execQueryRequest);
}