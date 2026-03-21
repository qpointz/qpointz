package io.qpointz.mill.ai.service

import io.qpointz.mill.excepions.statuses.MillStatus
import io.qpointz.mill.excepions.statuses.MillStatusDetails
import io.qpointz.mill.excepions.statuses.MillStatusException
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [MillStatusException] and [MillStatusRuntimeException] to HTTP responses
 * for the AI chat API surface.
 *
 * HTTP status mapping follows the platform convention (WI-083):
 * - BAD_REQUEST → 400
 * - UNAUTHORIZED → 401
 * - FORBIDDEN → 403
 * - NOT_FOUND → 404
 * - CONFLICT → 409
 * - UNPROCESSABLE → 422
 * - TOO_MANY_REQUESTS → 429
 * - INTERNAL_ERROR → 500
 */
@RestControllerAdvice(assignableTypes = [AiChatController::class])
class AiChatExceptionHandler {

    @ExceptionHandler(MillStatusException::class)
    fun handleMillStatusException(ex: MillStatusException): ResponseEntity<MillStatusDetails> =
        ResponseEntity(MillStatusDetails.of(ex), httpStatus(ex.status()))

    @ExceptionHandler(MillStatusRuntimeException::class)
    fun handleMillStatusRuntimeException(ex: MillStatusRuntimeException): ResponseEntity<MillStatusDetails> =
        ResponseEntity(MillStatusDetails.of(ex), httpStatus(ex.status()))

    private fun httpStatus(status: MillStatus): HttpStatus =
        when (status) {
            MillStatus.BAD_REQUEST -> HttpStatus.BAD_REQUEST
            MillStatus.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
            MillStatus.FORBIDDEN -> HttpStatus.FORBIDDEN
            MillStatus.NOT_FOUND -> HttpStatus.NOT_FOUND
            MillStatus.CONFLICT -> HttpStatus.CONFLICT
            MillStatus.UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY
            MillStatus.TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS
            MillStatus.INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
        }
}
