package io.qpointz.mill.data.schema.api

import io.qpointz.mill.excepions.statuses.MillStatus
import io.qpointz.mill.excepions.statuses.MillStatusDetails
import io.qpointz.mill.excepions.statuses.MillStatusException
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates Mill status exceptions to HTTP responses for schema explorer endpoints.
 */
@RestControllerAdvice
class SchemaExceptionHandler {

    /**
     * Handles checked status exceptions.
     *
     * @param ex thrown exception
     * @return mapped status response
     */
    @ExceptionHandler(MillStatusException::class)
    fun handleMillStatusException(ex: MillStatusException): ResponseEntity<MillStatusDetails> =
        ResponseEntity(MillStatusDetails.of(ex), httpStatus(ex.status()))

    /**
     * Handles runtime status exceptions.
     *
     * @param ex thrown exception
     * @return mapped status response
     */
    @ExceptionHandler(MillStatusRuntimeException::class)
    fun handleMillStatusRuntimeException(ex: MillStatusRuntimeException): ResponseEntity<MillStatusDetails> =
        ResponseEntity(MillStatusDetails.of(ex), httpStatus(ex.status()))

    /**
     * Maps [MillStatus] values to HTTP statuses.
     *
     * @param status Mill status enum
     * @return mapped Spring HTTP status
     */
    private fun httpStatus(status: MillStatus): HttpStatus = when (status) {
        MillStatus.NOT_FOUND -> HttpStatus.NOT_FOUND
        MillStatus.BAD_REQUEST -> HttpStatus.BAD_REQUEST
        MillStatus.CONFLICT -> HttpStatus.CONFLICT
        MillStatus.FORBIDDEN -> HttpStatus.FORBIDDEN
        MillStatus.UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY
        MillStatus.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
        MillStatus.TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS
        MillStatus.INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
    }
}
