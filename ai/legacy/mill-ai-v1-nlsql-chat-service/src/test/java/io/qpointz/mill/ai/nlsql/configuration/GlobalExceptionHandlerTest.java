package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.excepions.statuses.MillStatuses;
import io.qpointz.mill.excepions.statuses.MillStatusException;
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleStatusException() - NOT_FOUND maps to 404")
    void shouldMapNotFoundTo404() throws MillStatusException {
        var ex = MillStatuses.notFound("Chat not found");
        ResponseEntity<Void> response = exceptionHandler.handleStatusException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("handleStatusException() - BAD_REQUEST maps to 400")
    void shouldMapBadRequestTo400() throws MillStatusException {
        var ex = MillStatuses.badRequest("Invalid input");
        ResponseEntity<Void> response = exceptionHandler.handleStatusException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("handleStatusException() - no-message overload uses enum name as message")
    void shouldFallbackToEnumName() throws MillStatusException {
        var ex = MillStatuses.notFound();
        assertEquals("NOT_FOUND", ex.getMessage());
        ResponseEntity<Void> response = exceptionHandler.handleStatusException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("handleStatusRuntimeException() - NOT_FOUND maps to 404")
    void shouldMapRuntimeNotFoundTo404() {
        var ex = MillStatuses.notFoundRuntime("Chat not found");
        ResponseEntity<Void> response = exceptionHandler.handleStatusRuntimeException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("handleStatusRuntimeException() - INTERNAL_ERROR maps to 500")
    void shouldMapInternalErrorTo500() {
        var ex = MillStatuses.internalErrorRuntime();
        ResponseEntity<Void> response = exceptionHandler.handleStatusRuntimeException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
