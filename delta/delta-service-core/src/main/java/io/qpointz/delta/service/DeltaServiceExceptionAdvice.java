package io.qpointz.delta.service;

import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;


@Slf4j
@GrpcAdvice
public class DeltaServiceExceptionAdvice {

    @GrpcExceptionHandler(StatusRuntimeException.class)
    public StatusRuntimeException handleRuntimeStatus(StatusRuntimeException e) {
        return e;
    }

    @GrpcExceptionHandler(StatusException.class)
    public StatusException handleRuntimeStatus(StatusException e) {
        return e;
    }

}
