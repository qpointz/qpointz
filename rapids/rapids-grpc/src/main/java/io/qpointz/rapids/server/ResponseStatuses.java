package io.qpointz.rapids.server;

import io.qpointz.rapids.grpc.ResponseCode;
import io.qpointz.rapids.grpc.ResponseStatus;

public class ResponseStatuses {

    public static ResponseStatus statusOk() {
        return status(ResponseCode.OK, "OK");
    }

    public static ResponseStatus statusError(String message) {
        return status(ResponseCode.ERROR, message);
    }

    public static ResponseStatus statusError(Throwable throwable) {
        return statusError(throwable.getMessage());
    }


    public static ResponseStatus status(ResponseCode code, String message) {
        return ResponseStatus.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build();
    }

    public static ResponseStatus statusInvalidRequest(String message, Object... args) {
        return status(ResponseCode.INVALID_REQUEST, String.format(message, args));
    }

}
