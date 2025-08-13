package io.qpointz.mill;

import java.io.Serial;
import java.io.Serializable;

public class MillException extends Exception implements Serializable {

    @Serial
    private static final long serialVersionUID = -767625465462L;

    public MillException() {
        super();
    }

    public MillException(String message) {
        super(message);
    }

    public MillException(String message, Throwable cause) {
        super(message, cause);
    }

    public MillException(Throwable cause) {
        super(cause);
    }
}
