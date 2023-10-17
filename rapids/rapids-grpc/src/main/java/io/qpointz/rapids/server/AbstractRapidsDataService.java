package io.qpointz.rapids.server;

import io.grpc.stub.StreamObserver;
import io.qpointz.rapids.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public abstract class AbstractRapidsDataService extends RapidsDataServiceGrpc.RapidsDataServiceImplBase implements RapidsDataServiceGrpc.AsyncService {

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

    protected <TRec, TResp> void process(TRec request, StreamObserver<TResp> response, Function<TRec, TResp> onHandle) {
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
        return DEFAULT_HANDSHAKE;
    }

    protected static ServiceVersion CURRENT_VERSION = ServiceVersion.V_10;

    protected static HandshakeResponse DEFAULT_HANDSHAKE = HandshakeResponse.newBuilder()
            .setStatus(ResponseStatus
                    .newBuilder()
                    .setCode(ResponseCode.OK)
                    .setMessage("OK")
                    .build())
            .setVersion(CURRENT_VERSION)
            .build();


    protected abstract GetCatalogResponse onGetCatalog(GetCatalogRequest getCatalogRequest) ;

}