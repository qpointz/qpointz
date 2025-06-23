package io.qpointz.mill.excepions.statuses;

import java.io.Serial;

public class MIllNotFoundStatusException extends MillStatusException {

    @Serial
    private static final long serialVersionUID = 1799363501104443050L;

    public MIllNotFoundStatusException() {
        super();
    }

    public MIllNotFoundStatusException(String message) {
        super(message);
    }

    public MIllNotFoundStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public MIllNotFoundStatusException(Throwable cause) {
        super(cause);
    }

}
