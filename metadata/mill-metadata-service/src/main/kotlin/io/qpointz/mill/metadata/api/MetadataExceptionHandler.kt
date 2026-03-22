package io.qpointz.mill.metadata.api

import io.qpointz.mill.excepions.statuses.MillStatus
import io.qpointz.mill.excepions.statuses.MillStatusDetails
import io.qpointz.mill.excepions.statuses.MillStatusException
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [MillStatusException], [MillStatusRuntimeException], and
 * [IllegalArgumentException] to HTTP responses for the metadata API surface.
 *
 * HTTP status mapping:
 * - [MillStatus.NOT_FOUND]         → 404 (entity or facet type not found)
 * - [MillStatus.BAD_REQUEST]       → 400 (malformed body or invalid YAML)
 * - [MillStatus.CONFLICT]          → 409 (delete of mandatory facet type)
 * - [MillStatus.FORBIDDEN]         → 403 (actor lacks scope permission)
 * - [MillStatus.UNPROCESSABLE]     → 422 (facet payload fails content-schema validation)
 * - Any other status               → 500 (unexpected error)
 * - [IllegalArgumentException]     → 409 (catalog constraint violation, e.g. mandatory delete)
 */
@RestControllerAdvice
class MetadataExceptionHandler {

    /**
     * Handles checked [MillStatusException] by mapping to the appropriate HTTP status.
     *
     * @param ex the caught exception
     * @return response entity with [MillStatusDetails] body and mapped HTTP status
     */
    @ExceptionHandler(MillStatusException::class)
    fun handleMillStatusException(ex: MillStatusException): ResponseEntity<MillStatusDetails> =
        ResponseEntity(MillStatusDetails.of(ex), httpStatus(ex.status()))

    /**
     * Handles unchecked [MillStatusRuntimeException] by mapping to the appropriate HTTP status.
     *
     * @param ex the caught exception
     * @return response entity with [MillStatusDetails] body and mapped HTTP status
     */
    @ExceptionHandler(MillStatusRuntimeException::class)
    fun handleMillStatusRuntimeException(ex: MillStatusRuntimeException): ResponseEntity<MillStatusDetails> =
        ResponseEntity(MillStatusDetails.of(ex), httpStatus(ex.status()))

    /**
     * Handles [IllegalArgumentException] thrown by catalog operations (e.g. attempting to delete
     * a mandatory facet type) by returning 409 Conflict.
     *
     * @param ex the caught exception
     * @return 409 response with the exception message as the body
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("message" to ex.message), HttpStatus.CONFLICT)

    /**
     * Maps a [MillStatus] to the corresponding Spring [HttpStatus].
     *
     * @param status the Mill status code
     * @return the HTTP status code for the response
     */
    private fun httpStatus(status: MillStatus): HttpStatus = when (status) {
        MillStatus.NOT_FOUND      -> HttpStatus.NOT_FOUND
        MillStatus.BAD_REQUEST    -> HttpStatus.BAD_REQUEST
        MillStatus.CONFLICT       -> HttpStatus.CONFLICT
        MillStatus.FORBIDDEN      -> HttpStatus.FORBIDDEN
        MillStatus.UNPROCESSABLE  -> HttpStatus.UNPROCESSABLE_ENTITY
        MillStatus.UNAUTHORIZED   -> HttpStatus.UNAUTHORIZED
        MillStatus.TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS
        MillStatus.INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
    }
}
