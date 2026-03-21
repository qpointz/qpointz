package io.qpointz.mill.excepions.statuses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MillStatusDetails {

    private final MillStatus status;
    private final String message;
    private final long timestamp;
    private final Map<String, Object> details;

    private MillStatusDetails(MillStatus status, String message, Map<String, Object> details) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.details = details;
    }

    public static MillStatusDetails of(MillStatusException ex) {
        return new MillStatusDetails(ex.status(), ex.getMessage(), null);
    }

    public static MillStatusDetails of(MillStatusRuntimeException ex) {
        return new MillStatusDetails(ex.status(), ex.getMessage(), null);
    }

    public static MillStatusDetails of(MillStatusException ex, Map<String, Object> details) {
        return new MillStatusDetails(ex.status(), ex.getMessage(), details);
    }

    public static MillStatusDetails of(MillStatusRuntimeException ex, Map<String, Object> details) {
        return new MillStatusDetails(ex.status(), ex.getMessage(), details);
    }
}
