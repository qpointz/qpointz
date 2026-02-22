package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.excepions.statuses.MIllNotFoundStatusException;
import io.qpointz.mill.data.backend.annotations.ConditionalOnService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles module-specific exceptions and maps them to HTTP responses.
 */
@RestControllerAdvice
@Configuration
@ConditionalOnService("ai-nl2data")
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * Maps domain not-found exceptions to HTTP 404 responses.
     *
     * @param ex domain not-found exception
     * @return framework error response payload
     */
    @ExceptionHandler(MIllNotFoundStatusException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(MIllNotFoundStatusException ex) {
        return ErrorResponse.create(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

}
