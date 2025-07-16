package io.qpointz.mill;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serial;

public class MillRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8931058914666470647L;

    public MillRuntimeException() {
        super();
    }

    public MillRuntimeException(String message) {
        super(message);
    }

    public MillRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MillRuntimeException(Throwable cause) {
        super(cause);
    }

    public static MillRuntimeException of(JsonProcessingException e) {
        return new MillRuntimeException(e);
    }
}