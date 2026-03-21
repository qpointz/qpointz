package io.qpointz.mill.excepions.statuses;

import io.qpointz.mill.MillRuntimeException;

import java.io.Serial;

public class MillStatusRuntimeException extends MillRuntimeException {

    @Serial
    private static final long serialVersionUID = 3921045872016773401L;

    private final MillStatus status;

    public MillStatusRuntimeException(MillStatus status, String message) {
        super(message);
        this.status = status;
    }

    public MillStatusRuntimeException(MillStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public MillStatusRuntimeException(MillStatus status) {
        super(status.name());
        this.status = status;
    }

    public MillStatusRuntimeException(MillStatus status, Throwable cause) {
        super(status.name(), cause);
        this.status = status;
    }

    public MillStatus status() {
        return status;
    }
}
