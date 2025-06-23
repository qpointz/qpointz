package io.qpointz.mill.excepions.statuses;

import io.qpointz.mill.MillException;

import java.io.Serial;
import java.io.Serializable;

public abstract class MillStatusException extends MillException implements Serializable {


    @Serial
    private static final long serialVersionUID = 1799363501104443050L;

    public MillStatusException() {
        super();
    }

    public MillStatusException(String message) {
        super(message);
    }

    public MillStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public MillStatusException(Throwable cause) {
        super(cause);
    }

}
