package io.qpointz.delta.service;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.qpointz.delta.proto.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class DeltaServiceBase {

    private DeltaServiceBase() {
    }

    private boolean supportsSql() {
        return this.sqlParserProvider != null && sqlParserProvider.getAcceptsSql()
                && this.executionProvider!=null && this.executionProvider.canExecuteSql();
    }

    private boolean supportsSubstraitPlan() {
        return this.getExecutionProvider() != null && this.executionProvider.canExecuteSubstraitPlan();
    }

    public static void streamResult(Iterator<VectorBlock> iterator, ServerCallStreamObserver<ExecQueryResponse> observer) {
        while (iterator.hasNext()) {
            val resp = ExecQueryResponse.newBuilder()
                    .setStatus(ResponseStatuses.ok())
                    .setVector(iterator.next());
            observer.onNext(resp.build());
        }
        observer.onCompleted();
    }

    public static <TResp> void process(StreamObserver<TResp> observer, TResp reply) {
        if (reply!=null) {
            observer.onNext(reply);
            observer.onCompleted();
        } else {
            observer.onError(new NullPointerException("Null response"));
        }
    }

    public static <TReq, TResp> void process(TReq request, StreamObserver<TResp> observer, Function<TReq, TResp> onHandle) {
        try {
            final var reply = onHandle.apply(request);
            process(observer, reply);
        } catch (Exception ex) {
            log.error("Server error", ex);
            observer.onError(ex);
        }
    }
}
