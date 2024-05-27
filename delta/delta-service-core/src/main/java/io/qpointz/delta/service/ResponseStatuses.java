package io.qpointz.delta.service;

import io.qpointz.delta.proto.ResponseCode;
import io.qpointz.delta.proto.ResponseStatus;
import lombok.val;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ResponseStatuses {

    static ResponseStatus error() {
        return error("ERROR");
    }

    static ResponseStatus error(String message) {
        return error(message, List.of());
    }

    static ResponseStatus error(Exception ex) {
        val errors = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
        return error(ex.getMessage(), errors);
    }

    static ResponseStatus error(String message, Collection<String> errors) {
        return of(ResponseCode.ERROR, message, errors);
    }

    static ResponseStatus invalidRequest() {
        return invalidRequest("INVALID REQUEST");
    }

    static ResponseStatus invalidRequest(String message) {
        return of(ResponseCode.INVALID_REQUEST, message);
    }

    static ResponseStatus ok() {return ok("OK");}

    static ResponseStatus ok(String message) {
        return of(ResponseCode.OK, message);
    }

    static ResponseStatus of(ResponseCode code, String message) {
        return of(code, message, List.of());
    }

    static ResponseStatus of(ResponseCode code, String message, Collection<String> errors) {
        return ResponseStatus.newBuilder()
                .setCode(code)
                .setMessage(message)
                .addAllErrors(errors)
                .build();
    }

}
