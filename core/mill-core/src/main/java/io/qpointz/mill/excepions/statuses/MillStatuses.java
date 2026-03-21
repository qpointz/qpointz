package io.qpointz.mill.excepions.statuses;

public class MillStatuses {

    // --- checked ---

    public static MillStatusException badRequest(String message) {
        return new MillStatusException(MillStatus.BAD_REQUEST, message);
    }

    public static MillStatusException badRequest() {
        return new MillStatusException(MillStatus.BAD_REQUEST);
    }

    public static MillStatusException unauthorized(String message) {
        return new MillStatusException(MillStatus.UNAUTHORIZED, message);
    }

    public static MillStatusException unauthorized() {
        return new MillStatusException(MillStatus.UNAUTHORIZED);
    }

    public static MillStatusException forbidden(String message) {
        return new MillStatusException(MillStatus.FORBIDDEN, message);
    }

    public static MillStatusException forbidden() {
        return new MillStatusException(MillStatus.FORBIDDEN);
    }

    public static MillStatusException notFound(String message) {
        return new MillStatusException(MillStatus.NOT_FOUND, message);
    }

    public static MillStatusException notFound() {
        return new MillStatusException(MillStatus.NOT_FOUND);
    }

    public static MillStatusException conflict(String message) {
        return new MillStatusException(MillStatus.CONFLICT, message);
    }

    public static MillStatusException conflict() {
        return new MillStatusException(MillStatus.CONFLICT);
    }

    public static MillStatusException unprocessable(String message) {
        return new MillStatusException(MillStatus.UNPROCESSABLE, message);
    }

    public static MillStatusException unprocessable() {
        return new MillStatusException(MillStatus.UNPROCESSABLE);
    }

    public static MillStatusException tooManyRequests(String message) {
        return new MillStatusException(MillStatus.TOO_MANY_REQUESTS, message);
    }

    public static MillStatusException tooManyRequests() {
        return new MillStatusException(MillStatus.TOO_MANY_REQUESTS);
    }

    public static MillStatusException internalError(String message) {
        return new MillStatusException(MillStatus.INTERNAL_ERROR, message);
    }

    public static MillStatusException internalError() {
        return new MillStatusException(MillStatus.INTERNAL_ERROR);
    }

    // --- unchecked ---

    public static MillStatusRuntimeException badRequestRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.BAD_REQUEST, message);
    }

    public static MillStatusRuntimeException badRequestRuntime() {
        return new MillStatusRuntimeException(MillStatus.BAD_REQUEST);
    }

    public static MillStatusRuntimeException unauthorizedRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.UNAUTHORIZED, message);
    }

    public static MillStatusRuntimeException unauthorizedRuntime() {
        return new MillStatusRuntimeException(MillStatus.UNAUTHORIZED);
    }

    public static MillStatusRuntimeException forbiddenRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.FORBIDDEN, message);
    }

    public static MillStatusRuntimeException forbiddenRuntime() {
        return new MillStatusRuntimeException(MillStatus.FORBIDDEN);
    }

    public static MillStatusRuntimeException notFoundRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.NOT_FOUND, message);
    }

    public static MillStatusRuntimeException notFoundRuntime() {
        return new MillStatusRuntimeException(MillStatus.NOT_FOUND);
    }

    public static MillStatusRuntimeException conflictRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.CONFLICT, message);
    }

    public static MillStatusRuntimeException conflictRuntime() {
        return new MillStatusRuntimeException(MillStatus.CONFLICT);
    }

    public static MillStatusRuntimeException unprocessableRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.UNPROCESSABLE, message);
    }

    public static MillStatusRuntimeException unprocessableRuntime() {
        return new MillStatusRuntimeException(MillStatus.UNPROCESSABLE);
    }

    public static MillStatusRuntimeException tooManyRequestsRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.TOO_MANY_REQUESTS, message);
    }

    public static MillStatusRuntimeException tooManyRequestsRuntime() {
        return new MillStatusRuntimeException(MillStatus.TOO_MANY_REQUESTS);
    }

    public static MillStatusRuntimeException internalErrorRuntime(String message) {
        return new MillStatusRuntimeException(MillStatus.INTERNAL_ERROR, message);
    }

    public static MillStatusRuntimeException internalErrorRuntime() {
        return new MillStatusRuntimeException(MillStatus.INTERNAL_ERROR);
    }
}
