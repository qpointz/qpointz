package io.qpointz.mill.services;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;


@Slf4j
@GrpcAdvice
public class MillServiceExceptionAdvice {

    public MillServiceExceptionAdvice() {
        log.trace("Mill Exception advice");
    }

    @GrpcExceptionHandler(StatusRuntimeException.class)
    public StatusRuntimeException handleRuntimeStatus(StatusRuntimeException e) {
        return e;
    }

    @GrpcExceptionHandler(StatusException.class)
    public StatusException handleRuntimeStatus(StatusException e) {
        return e;
    }

    @GrpcExceptionHandler(Error.class)
    public StatusException handeError(Error e) {
        log.error("Intercepted error:\r", e);
        return Status.INTERNAL
                .withDescription(String.format("ERROR:%s", e.getMessage()))
                .withCause(e)
                .asException();
    }

}
