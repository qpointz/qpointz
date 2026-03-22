package io.qpointz.mill.metadata.api

import io.qpointz.mill.excepions.statuses.MillStatus
import io.qpointz.mill.excepions.statuses.MillStatusException
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MetadataExceptionHandlerTest {

    private val handler = MetadataExceptionHandler()

    @Test
    fun shouldReturn404_whenNotFoundStatus() {
        val ex = MillStatusRuntimeException(MillStatus.NOT_FOUND, "entity not found")
        val response = handler.handleMillStatusRuntimeException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldReturn400_whenBadRequestStatus() {
        val ex = MillStatusRuntimeException(MillStatus.BAD_REQUEST, "invalid input")
        val response = handler.handleMillStatusRuntimeException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun shouldReturn409_whenConflictStatus() {
        val ex = MillStatusRuntimeException(MillStatus.CONFLICT, "already exists")
        val response = handler.handleMillStatusRuntimeException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun shouldReturn403_whenForbiddenStatus() {
        val ex = MillStatusRuntimeException(MillStatus.FORBIDDEN, "access denied")
        val response = handler.handleMillStatusRuntimeException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun shouldReturn422_whenUnprocessableStatus() {
        val ex = MillStatusRuntimeException(MillStatus.UNPROCESSABLE, "validation failed")
        val response = handler.handleMillStatusRuntimeException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
    }

    @Test
    fun shouldReturn500_whenInternalErrorStatus() {
        val ex = MillStatusRuntimeException(MillStatus.INTERNAL_ERROR, "unexpected")
        val response = handler.handleMillStatusRuntimeException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun shouldHandleCheckedMillStatusException_whenNotFoundStatus() {
        val ex = MillStatusException(MillStatus.NOT_FOUND, "checked not found")
        val response = handler.handleMillStatusException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldReturn409_whenIllegalArgumentExceptionThrown() {
        val ex = IllegalArgumentException("Cannot delete mandatory facet type")
        val response = handler.handleIllegalArgumentException(ex)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body?.get("message")).contains("mandatory")
    }
}
