package io.qpointz.mill.excepions.statuses;

public class MillStatuses {

    public static MIllNotFoundStatusException notFound(String message) {
        return new MIllNotFoundStatusException(message);
    }

}
