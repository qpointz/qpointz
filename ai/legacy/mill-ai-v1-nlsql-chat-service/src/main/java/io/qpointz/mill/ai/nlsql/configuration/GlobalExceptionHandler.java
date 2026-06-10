package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.excepions.statuses.MillStatusException;
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles module-specific exceptions and maps them to HTTP responses.
 */
@RestControllerAdvice
@Configuration
@ConditionalOnService("ai-nl2data")
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(MillStatusException.class)
    public ResponseEntity<Void> handleStatusException(MillStatusException ex) {
        return ResponseEntity.status(toHttpStatus(ex.status())).build();
    }

    @ExceptionHandler(MillStatusRuntimeException.class)
    public ResponseEntity<Void> handleStatusRuntimeException(MillStatusRuntimeException ex) {
        return ResponseEntity.status(toHttpStatus(ex.status())).build();
    }

    private HttpStatusCode toHttpStatus(io.qpointz.mill.excepions.statuses.MillStatus status) {
        return switch (status) {
            case BAD_REQUEST      -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED     -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN        -> HttpStatus.FORBIDDEN;
            case NOT_FOUND        -> HttpStatus.NOT_FOUND;
            case CONFLICT         -> HttpStatus.CONFLICT;
            case UNPROCESSABLE    -> HttpStatus.UNPROCESSABLE_ENTITY;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case INTERNAL_ERROR   -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
