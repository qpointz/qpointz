package io.qpointz.mill.excepions.statuses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MillStatusesTest {

    // --- checked ---

    @Test
    void shouldCreateBadRequest_whenMessageProvided() {
        var ex = MillStatuses.badRequest("invalid input");
        assertEquals(MillStatus.BAD_REQUEST, ex.status());
        assertEquals("invalid input", ex.getMessage());
    }

    @Test
    void shouldCreateBadRequest_whenNoMessage() {
        var ex = MillStatuses.badRequest();
        assertEquals(MillStatus.BAD_REQUEST, ex.status());
        assertEquals("BAD_REQUEST", ex.getMessage());
    }

    @Test
    void shouldCreateUnauthorized_whenMessageProvided() {
        var ex = MillStatuses.unauthorized("not authenticated");
        assertEquals(MillStatus.UNAUTHORIZED, ex.status());
        assertEquals("not authenticated", ex.getMessage());
    }

    @Test
    void shouldCreateUnauthorized_whenNoMessage() {
        var ex = MillStatuses.unauthorized();
        assertEquals(MillStatus.UNAUTHORIZED, ex.status());
        assertEquals("UNAUTHORIZED", ex.getMessage());
    }

    @Test
    void shouldCreateForbidden_whenMessageProvided() {
        var ex = MillStatuses.forbidden("access denied");
        assertEquals(MillStatus.FORBIDDEN, ex.status());
        assertEquals("access denied", ex.getMessage());
    }

    @Test
    void shouldCreateForbidden_whenNoMessage() {
        var ex = MillStatuses.forbidden();
        assertEquals(MillStatus.FORBIDDEN, ex.status());
        assertEquals("FORBIDDEN", ex.getMessage());
    }

    @Test
    void shouldCreateNotFound_whenMessageProvided() {
        var ex = MillStatuses.notFound("chat not found");
        assertEquals(MillStatus.NOT_FOUND, ex.status());
        assertEquals("chat not found", ex.getMessage());
    }

    @Test
    void shouldCreateNotFound_whenNoMessage() {
        var ex = MillStatuses.notFound();
        assertEquals(MillStatus.NOT_FOUND, ex.status());
        assertEquals("NOT_FOUND", ex.getMessage());
    }

    @Test
    void shouldCreateConflict_whenMessageProvided() {
        var ex = MillStatuses.conflict("already exists");
        assertEquals(MillStatus.CONFLICT, ex.status());
        assertEquals("already exists", ex.getMessage());
    }

    @Test
    void shouldCreateConflict_whenNoMessage() {
        var ex = MillStatuses.conflict();
        assertEquals(MillStatus.CONFLICT, ex.status());
        assertEquals("CONFLICT", ex.getMessage());
    }

    @Test
    void shouldCreateUnprocessable_whenMessageProvided() {
        var ex = MillStatuses.unprocessable("validation failed");
        assertEquals(MillStatus.UNPROCESSABLE, ex.status());
        assertEquals("validation failed", ex.getMessage());
    }

    @Test
    void shouldCreateUnprocessable_whenNoMessage() {
        var ex = MillStatuses.unprocessable();
        assertEquals(MillStatus.UNPROCESSABLE, ex.status());
        assertEquals("UNPROCESSABLE", ex.getMessage());
    }

    @Test
    void shouldCreateTooManyRequests_whenMessageProvided() {
        var ex = MillStatuses.tooManyRequests("rate limit exceeded");
        assertEquals(MillStatus.TOO_MANY_REQUESTS, ex.status());
        assertEquals("rate limit exceeded", ex.getMessage());
    }

    @Test
    void shouldCreateTooManyRequests_whenNoMessage() {
        var ex = MillStatuses.tooManyRequests();
        assertEquals(MillStatus.TOO_MANY_REQUESTS, ex.status());
        assertEquals("TOO_MANY_REQUESTS", ex.getMessage());
    }

    @Test
    void shouldCreateInternalError_whenMessageProvided() {
        var ex = MillStatuses.internalError("unexpected failure");
        assertEquals(MillStatus.INTERNAL_ERROR, ex.status());
        assertEquals("unexpected failure", ex.getMessage());
    }

    @Test
    void shouldCreateInternalError_whenNoMessage() {
        var ex = MillStatuses.internalError();
        assertEquals(MillStatus.INTERNAL_ERROR, ex.status());
        assertEquals("INTERNAL_ERROR", ex.getMessage());
    }

    // --- unchecked ---

    @Test
    void shouldCreateNotFoundRuntime_whenMessageProvided() {
        var ex = MillStatuses.notFoundRuntime("chat not found");
        assertEquals(MillStatus.NOT_FOUND, ex.status());
        assertEquals("chat not found", ex.getMessage());
    }

    @Test
    void shouldCreateNotFoundRuntime_whenNoMessage() {
        var ex = MillStatuses.notFoundRuntime();
        assertEquals(MillStatus.NOT_FOUND, ex.status());
        assertEquals("NOT_FOUND", ex.getMessage());
    }

    @Test
    void shouldCreateInternalErrorRuntime_whenMessageProvided() {
        var ex = MillStatuses.internalErrorRuntime("unexpected failure");
        assertEquals(MillStatus.INTERNAL_ERROR, ex.status());
        assertEquals("unexpected failure", ex.getMessage());
    }

    @Test
    void shouldCreateInternalErrorRuntime_whenNoMessage() {
        var ex = MillStatuses.internalErrorRuntime();
        assertEquals(MillStatus.INTERNAL_ERROR, ex.status());
        assertEquals("INTERNAL_ERROR", ex.getMessage());
    }

    @Test
    void shouldPreserveCause_whenCauseProvided() {
        var cause = new RuntimeException("root cause");
        var ex = new MillStatusException(MillStatus.INTERNAL_ERROR, "wrapped", cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    void shouldPreserveCause_whenRuntimeCauseProvided() {
        var cause = new RuntimeException("root cause");
        var ex = new MillStatusRuntimeException(MillStatus.INTERNAL_ERROR, "wrapped", cause);
        assertEquals(cause, ex.getCause());
    }
}
