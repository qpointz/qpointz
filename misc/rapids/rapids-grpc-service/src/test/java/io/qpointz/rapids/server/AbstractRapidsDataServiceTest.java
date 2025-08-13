package io.qpointz.rapids.server;

import io.grpc.stub.StreamObserver;
import io.qpointz.rapids.grpc.*;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class   AbstractRapidsDataServiceTest {

    @Test
    void processCall() {
        final var observer = (StreamObserver<ListCatalogResponse>)mock(StreamObserver.class);

        final var request = ListCatalogRequest.newBuilder().build();
        final var resp = ListCatalogResponse.newBuilder().build();

        final var service = Mockito.mock(AbstractRapidsDataService.class, Answers.CALLS_REAL_METHODS);
        when(service.onListCatalogs(request)).thenReturn(resp);

        service.listCatalogs(request, observer);

        final var inorder = inOrder(observer);
        inorder.verify(observer, times(1)).onNext(resp);
        inorder.verify(observer, times(1)).onCompleted();
        inorder.verify(observer, never()).onError(isA(Exception.class));
    }

    @Test
    void processCallsOnErorr() {
        final var observer = (StreamObserver<GetCatalogResponse>)mock(StreamObserver.class);

        final var service = Mockito.mock(AbstractRapidsDataService.class, Answers.CALLS_REAL_METHODS);
        final var request = GetCatalogRequest.newBuilder().build();
        final var throwable = new IllegalArgumentException();
        when(service.onGetCatalog(request)).thenThrow(throwable);
        service.getCatalog(request, observer);
        verify(observer, times(1)).onError(throwable);
        verify(observer, never()).onNext(isA(GetCatalogResponse.class));
        verify(observer, never()).onCompleted();
    }

    @Test
    void processCallsOnErrorOnNullReply() {
        final var observer = (StreamObserver<GetCatalogResponse>)mock(StreamObserver.class);
        final var service = Mockito.mock(AbstractRapidsDataService.class, Answers.CALLS_REAL_METHODS);
        when(service.onGetCatalog(isA(GetCatalogRequest.class))).thenReturn(null);

        service.getCatalog(GetCatalogRequest.getDefaultInstance(), observer);
        verify(observer, times(1)).onError(isA(NullPointerException.class));
        verify(observer, never()).onNext(isA(GetCatalogResponse.class));
        verify(observer, never()).onCompleted();

    }
}