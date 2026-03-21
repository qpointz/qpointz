package io.qpointz.mill.excepions.statuses;

import io.qpointz.mill.MillException;

import java.io.Serial;

public class MillStatusException extends MillException {

    @Serial
    private static final long serialVersionUID = 1799363501104443050L;

    private final MillStatus status;

    public MillStatusException(MillStatus status, String message) {
        super(message);
        this.status = status;
    }

    public MillStatusException(MillStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public MillStatusException(MillStatus status) {
        super(status.name());
        this.status = status;
    }

    public MillStatusException(MillStatus status, Throwable cause) {
        super(status.name(), cause);
        this.status = status;
    }

    public MillStatus status() {
        return status;
    }
}
