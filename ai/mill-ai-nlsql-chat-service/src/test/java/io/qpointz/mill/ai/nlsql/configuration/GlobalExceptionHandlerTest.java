package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.excepions.statuses.MIllNotFoundStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleNotFoundException() - Should return ErrorResponse with correct status and message")
    void testHandleNotFoundException() {
        // Given
        String errorMessage = "Chat not found";
        MIllNotFoundStatusException exception = new MIllNotFoundStatusException(errorMessage);

        // When
        ErrorResponse response = exceptionHandler.handleNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("handleNotFoundException() - Should handle exception with null message")
    void testHandleNotFoundExceptionWithNullMessage() {
        // Given
        MIllNotFoundStatusException exception = new MIllNotFoundStatusException();

        // When
        ErrorResponse response = exceptionHandler.handleNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

    @Test
    @DisplayName("handleNotFoundException() - Should handle exception with custom message")
    void testHandleNotFoundExceptionWithCustomMessage() {
        // Given
        String customMessage = "User not found in database";
        MIllNotFoundStatusException exception = new MIllNotFoundStatusException(customMessage);

        // When
        ErrorResponse response = exceptionHandler.handleNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("handleNotFoundException() - Should handle exception with cause")
    void testHandleNotFoundExceptionWithCause() {
        // Given
        String errorMessage = "Database connection failed";
        RuntimeException cause = new RuntimeException("Connection timeout");
        MIllNotFoundStatusException exception = new MIllNotFoundStatusException(errorMessage, cause);

        // When
        ErrorResponse response = exceptionHandler.handleNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("handleNotFoundException() - Should return ErrorResponse with correct structure")
    void testHandleNotFoundExceptionResponseStructure() {
        // Given
        String errorMessage = "Resource not found";
        MIllNotFoundStatusException exception = new MIllNotFoundStatusException(errorMessage);

        // When
        ErrorResponse response = exceptionHandler.handleNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        
        // Verify ErrorResponse structure
        assertThat(response.getBody().getStatus(), is(404));
    }

    @Test
    @DisplayName("handleNotFoundException() - Should handle multiple exceptions independently")
    void testHandleMultipleExceptions() {
        // Given
        MIllNotFoundStatusException exception1 = new MIllNotFoundStatusException("First error");
        MIllNotFoundStatusException exception2 = new MIllNotFoundStatusException("Second error");

        // When
        ErrorResponse response1 = exceptionHandler.handleNotFoundException(exception1);
        ErrorResponse response2 = exceptionHandler.handleNotFoundException(exception2);

        // Then
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotSame(response1, response2);
    }
}
