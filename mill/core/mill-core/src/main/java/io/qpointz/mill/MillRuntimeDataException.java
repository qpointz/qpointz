package io.qpointz.mill;

import java.io.Serial;

public class MillRuntimeDataException extends MillRuntimeException {

    @Serial
    private static final long serialVersionUID = -92678462L;

    public MillRuntimeDataException() {
        super();
    }

    public MillRuntimeDataException(String message) {
        super(message);
    }

    public MillRuntimeDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public MillRuntimeDataException(Throwable cause) {
        super(cause);
    }

}
