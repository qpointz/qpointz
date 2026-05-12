package io.qpointz.mill.data.query.web

import io.qpointz.mill.data.query.engine.QueryEpochConflictException
import io.qpointz.mill.data.query.engine.QuerySessionForbiddenException
import io.qpointz.mill.data.query.engine.QuerySessionNotFoundException
import io.qpointz.mill.data.query.engine.QuerySqlExecutionException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.json.JsonMapper

/**
 * Maps core query-result exceptions to HTTP responses with a small structured error body.
 *
 * @param jsonMapper JSON encoder for error payloads
 */
@RestControllerAdvice(basePackageClasses = [QueryResultRestController::class])
class QueryResultExceptionHandler(
    private val jsonMapper: JsonMapper,
) {

    /**
     * @param ex controller-level status (for example unsatisfiable {@code Accept})
     */
    @ExceptionHandler(ResponseStatusException::class)
    fun responseStatus(ex: ResponseStatusException): ResponseEntity<String> {
        val status = HttpStatus.resolve(ex.statusCode.value()) ?: HttpStatus.INTERNAL_SERVER_ERROR
        return error(status, "http", ex.reason ?: status.reasonPhrase)
    }

    /**
     * @param ex unknown session id
     */
    @ExceptionHandler(QuerySessionNotFoundException::class)
    fun notFound(ex: QuerySessionNotFoundException): ResponseEntity<String> =
        error(HttpStatus.NOT_FOUND, "not_found", ex.message ?: "Not found")

    /**
     * @param ex tenant mismatch on a known session
     */
    @ExceptionHandler(QuerySessionForbiddenException::class)
    fun forbidden(ex: QuerySessionForbiddenException): ResponseEntity<String> =
        error(HttpStatus.FORBIDDEN, "forbidden", ex.message ?: "Forbidden")

    /**
     * @param ex stale optimistic epoch on rows read
     */
    @ExceptionHandler(QueryEpochConflictException::class)
    fun conflict(ex: QueryEpochConflictException): ResponseEntity<String> =
        error(
            HttpStatus.CONFLICT,
            "stale_epoch",
            "epoch mismatch: client=${ex.clientEpoch} server=${ex.serverEpoch}",
        )

    /**
     * @param ex SQL or engine failure after a well-formed request
     */
    @ExceptionHandler(QuerySqlExecutionException::class)
    fun unprocessable(ex: QuerySqlExecutionException): ResponseEntity<String> =
        error(HttpStatus.UNPROCESSABLE_ENTITY, "sql_execution_failed", ex.message ?: "Execution failed")

    /**
     * @param ex invalid paging, unknown format at core boundary, etc.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException): ResponseEntity<String> =
        error(HttpStatus.BAD_REQUEST, "bad_request", ex.message ?: "Bad request")

    private fun error(status: HttpStatus, code: String, message: String): ResponseEntity<String> {
        val body = linkedMapOf("code" to code, "error" to message)
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonMapper.writeValueAsString(body))
    }
}
