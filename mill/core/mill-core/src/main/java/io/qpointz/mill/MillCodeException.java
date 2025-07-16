package io.qpointz.mill;

import java.io.Serial;
import java.sql.SQLException;

public class MillCodeException extends MillException {

    @Serial
    private static final long serialVersionUID = -19289754209L;

    public MillCodeException() {
        super();
    }

    public MillCodeException(String message) {
        super(message);
    }

    public MillCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MillCodeException(Throwable cause) {
        super(cause);
    }

    public SQLException asSqlException() {
        return new SQLException(this);
    }
}
