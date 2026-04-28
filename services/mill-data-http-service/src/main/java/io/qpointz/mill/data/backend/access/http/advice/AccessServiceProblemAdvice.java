package io.qpointz.mill.data.backend.access.http.advice;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.data.backend.access.http.controllers.AccessServiceController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.UUID;

/**
 * Central RFC 9457 Problem Details responses for Mill data HTTP endpoints.
 *
 * <p>Legacy callers may rely on GRPC-shaped JSON (<code>code</code> + {@code message});
 * {@link ProblemDetail#setProperty(String, Object)} exposes those alongside standard fields.</p>
 */
@Slf4j
@RestControllerAdvice(assignableTypes = AccessServiceController.class)
public class AccessServiceProblemAdvice {

    private static ResponseEntity<ProblemDetail> wrap(HttpStatus http, ProblemDetail body) {
        return ResponseEntity
                .status(http)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private static String resolveTraceId(HttpServletRequest request) {
        val h1 = request.getHeader("X-Trace-Id");
        val h2 = request.getHeader("X-Correlation-Id");
        if (h1 != null && !h1.isBlank()) {
            return h1.trim();
        }
        if (h2 != null && !h2.isBlank()) {
            return h2.trim();
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Maps GRPC failures from the dispatcher to HTTP statuses and RFC 9457 payloads.
     *
     * @param ex GRPC status wrapper from backend operations
     * @param request current servlet request (for trace correlation headers)
     */
    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ProblemDetail> handleGrpcStatus(StatusRuntimeException ex, HttpServletRequest request) {
        val grpcStatus = ex.getStatus();
        val http = toHttpStatus(grpcStatus.getCode());
        val detail = grpcStatus.getDescription() == null || grpcStatus.getDescription().isBlank()
                ? grpcStatus.getCode().name()
                : grpcStatus.getDescription();
        val traceId = resolveTraceId(request);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(http, detail);
        pd.setTitle(titleForGrpc(grpcStatus.getCode()));
        pd.setType(URI.create("urn:mill:grpc:" + grpcStatus.getCode().name()));
        pd.setInstance(URI.create(safeInstanceUri(request.getRequestURI())));
        pd.setProperty("traceId", traceId);
        pd.setProperty("code", grpcStatus.getCode().name());
        pd.setProperty("message", detail);
        return wrap(http, pd);
    }

    private static String titleForGrpc(Status.Code code) {
        return switch (code) {
            case INVALID_ARGUMENT -> "Bad request";
            case NOT_FOUND -> "Not found";
            case ALREADY_EXISTS -> "Conflict";
            case PERMISSION_DENIED -> "Forbidden";
            case UNAUTHENTICATED -> "Unauthorized";
            case UNIMPLEMENTED -> "Not implemented";
            default -> "Request failed";
        };
    }

    /**
     * Parsing / protobuf bridging errors surfaced as validation-style failures.
     *
     * @param ex payload or conversion failure from {@link io.qpointz.mill.data.backend.access.http.components.MessageHelper}
     * @param request current servlet request (for trace correlation headers)
     */
    @ExceptionHandler(MillRuntimeException.class)
    public ResponseEntity<ProblemDetail> handleMillRuntime(MillRuntimeException ex, HttpServletRequest request) {
        val traceId = resolveTraceId(request);
        val msg = ex.getMessage() == null ? "Request failed" : ex.getMessage();
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        pd.setTitle("Invalid payload");
        pd.setType(URI.create("urn:mill:error:payload"));
        pd.setInstance(URI.create(safeInstanceUri(request.getRequestURI())));
        pd.setProperty("traceId", traceId);
        return wrap(HttpStatus.BAD_REQUEST, pd);
    }

    /**
     * Last-resort handler; hides exception details behind a stable client-facing message while logging internally.
     *
     * @param ex unexpected failure
     * @param request current servlet request (for trace correlation headers)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on data HTTP access path", ex);
        val traceId = resolveTraceId(request);
        val msg = "Internal error";
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, msg);
        pd.setTitle("Internal error");
        pd.setType(URI.create("urn:mill:error:internal"));
        pd.setInstance(URI.create(safeInstanceUri(request.getRequestURI())));
        pd.setProperty("traceId", traceId);
        return wrap(HttpStatus.INTERNAL_SERVER_ERROR, pd);
    }

    private HttpStatus toHttpStatus(Status.Code code) {
        return switch (code) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case UNIMPLEMENTED -> HttpStatus.NOT_IMPLEMENTED;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * RFC 9457 {@code instance} is an absolute or relative URI reference as string.
     */
    private static String safeInstanceUri(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return "/";
        }
        if (requestUri.startsWith("/")) {
            return requestUri;
        }
        return "/" + requestUri;
    }
}
