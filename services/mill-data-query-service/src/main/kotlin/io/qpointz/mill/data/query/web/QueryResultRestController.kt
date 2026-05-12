package io.qpointz.mill.data.query.web

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.data.query.engine.CreateSessionResult
import io.qpointz.mill.data.query.engine.PagedQueryPayload
import io.qpointz.mill.data.query.engine.QueryFormats
import io.qpointz.mill.data.query.engine.QueryResultExecutionService
import io.qpointz.mill.data.query.engine.SessionMetadata
import io.qpointz.mill.data.query.engine.marshal.ResultMarshaller
import io.qpointz.mill.data.query.engine.marshal.ResultMarshallerRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.json.JsonMapper

/**
 * HTTP execution sessions for materialized query results under {@code /api/v1/query}.
 *
 * @param executionService core session engine
 * @param marshallerRegistry SPI-backed marshaller lookup (format negotiation)
 * @param jsonMapper shared JSON mapper for envelope assembly
 */
@RestController
@RequestMapping("/api/v1/query")
@ConditionalOnService(value = "query", group = "data")
@Tag(name = "query-result", description = "Paged query-result execution sessions")
class QueryResultRestController(
    private val executionService: QueryResultExecutionService,
    private val marshallerRegistry: ResultMarshallerRegistry,
    private val jsonMapper: JsonMapper,
) {

    /**
     * @param authentication authenticated principal (tenant source)
     * @param body create request with SQL and optional first page
     */
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create a query-result session")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Session created and first page included"),
            ApiResponse(responseCode = "201", description = "Session created (metadata only)"),
            ApiResponse(responseCode = "400", description = "Malformed body or limits"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "422", description = "SQL or plan cannot execute"),
        ],
    )
    fun create(
        authentication: Authentication?,
        @RequestBody body: CreateQueryRequest,
    ): ResponseEntity<String> {
        val caller = requireCallerContext(authentication)
        if (body.sql.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "sql is required")
        }
        body.defaultFormat?.let { validateFormatId(it) }
        val created = executionService.create(
            caller,
            body.sql.trim(),
            body.defaultFormat,
            body.includeFirstPage,
            body.firstPageSize,
        )
        val status = if (created.firstPage == null) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonMapper.writeValueAsString(createResponseJson(created)))
    }

    /**
     * @param authentication authenticated principal
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @Operation(summary = "List query sessions (not supported)")
    fun listNotSupported(authentication: Authentication?) {
        requireCallerContext(authentication)
    }

    /**
     * @param authentication authenticated principal
     * @param executionId session id
     */
    @GetMapping(path = ["/{executionId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get session metadata")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Metadata returned"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Wrong tenant for known session"),
            ApiResponse(responseCode = "404", description = "Unknown session"),
        ],
    )
    fun metadata(
        authentication: Authentication?,
        @Parameter(description = "Opaque execution session id") @PathVariable executionId: String,
    ): ResponseEntity<String> {
        val caller = requireCallerContext(authentication)
        val meta = executionService.metadata(caller, executionId)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonMapper.writeValueAsString(metadataBody(meta)))
    }

    /**
     * @param authentication authenticated principal
     * @param executionId session id
     * @param pageIndex zero-based page index
     * @param pageSize presentation page size
     * @param format optional marshaller format id
     * @param accept optional Accept header (matched when format is absent)
     * @param epoch optional optimistic concurrency check
     */
    @GetMapping(path = ["/{executionId}/rows"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Fetch one paged result slice")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Page returned"),
            ApiResponse(responseCode = "400", description = "Invalid paging or unknown format"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Wrong tenant for known session"),
            ApiResponse(responseCode = "404", description = "Unknown session"),
            ApiResponse(responseCode = "406", description = "Accept cannot be satisfied"),
            ApiResponse(responseCode = "409", description = "Stale epoch"),
        ],
    )
    fun rows(
        authentication: Authentication?,
        @Parameter(description = "Opaque execution session id") @PathVariable executionId: String,
        @RequestParam(name = "pageIndex", defaultValue = "0") pageIndex: Int,
        @RequestParam(name = "pageSize", defaultValue = "50") pageSize: Int,
        @RequestParam(name = "format", required = false) format: String?,
        @RequestHeader(name = "Accept", required = false) accept: String?,
        @RequestParam(name = "epoch", required = false) epoch: Int?,
    ): ResponseEntity<String> {
        val caller = requireCallerContext(authentication)
        val meta = executionService.metadata(caller, executionId)
        val formatId = resolveFormatId(format, accept, meta.defaultFormat ?: QueryFormats.ROWS_OBJECTS)
        val page = executionService.getPage(
            caller,
            executionId,
            pageIndex,
            pageSize,
            formatId,
            epoch,
        )
        val marshaller = marshallerRegistry.byFormatId(formatId)!!
        val body = rowsEnvelope(page, marshaller)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(marshaller.contentType))
            .body(jsonMapper.writeValueAsString(body))
    }

    /**
     * @param authentication authenticated principal
     * @param executionId session id
     * @param body replacement SQL
     */
    @PutMapping(path = ["/{executionId}"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Replace SQL for an existing session")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Replaced"),
            ApiResponse(responseCode = "400", description = "Malformed body"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Wrong tenant for known session"),
            ApiResponse(responseCode = "404", description = "Unknown session"),
            ApiResponse(responseCode = "422", description = "SQL or plan cannot execute"),
        ],
    )
    fun replace(
        authentication: Authentication?,
        @Parameter(description = "Opaque execution session id") @PathVariable executionId: String,
        @RequestBody body: ReplaceQueryRequest,
    ): ResponseEntity<String> {
        val caller = requireCallerContext(authentication)
        if (body.sql.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "sql is required")
        }
        body.defaultFormat?.let { validateFormatId(it) }
        val replaced = executionService.replace(caller, executionId, body.sql.trim(), body.defaultFormat)
        val payload = linkedMapOf<String, Any?>("epoch" to replaced.epoch)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonMapper.writeValueAsString(payload))
    }

    /**
     * @param authentication authenticated principal
     * @param executionId session id
     */
    @DeleteMapping(path = ["/{executionId}"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a session and release buffers")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Deleted"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Wrong tenant for known session"),
            ApiResponse(responseCode = "404", description = "Unknown session"),
        ],
    )
    fun delete(
        authentication: Authentication?,
        @Parameter(description = "Opaque execution session id") @PathVariable executionId: String,
    ) {
        val caller = requireCallerContext(authentication)
        executionService.delete(caller, executionId)
    }

    private fun validateFormatId(formatId: String) {
        if (marshallerRegistry.byFormatId(formatId) == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown format: $formatId")
        }
    }

    private fun resolveFormatId(
        formatParam: String?,
        accept: String?,
        sessionDefault: String,
    ): String {
        if (!formatParam.isNullOrBlank()) {
            val id = formatParam.trim().lowercase()
            if (marshallerRegistry.byFormatId(id) == null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown format: $formatParam")
            }
            return id
        }
        if (!accept.isNullOrBlank()) {
            val m = resolveByAccept(accept, sessionDefault)
                ?: throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Accept cannot be satisfied")
            return m.formatId.lowercase()
        }
        val def = sessionDefault.lowercase()
        return if (marshallerRegistry.byFormatId(def) != null) {
            def
        } else {
            QueryFormats.ROWS_OBJECTS
        }
    }

    private fun resolveByAccept(acceptHeader: String, sessionDefault: String): ResultMarshaller? {
        val tokens = acceptHeader.split(",").map { it.trim().substringBefore(";").trim().lowercase() }
        if (tokens.any { it == "*/*" }) {
            return marshallerRegistry.byFormatId(sessionDefault.lowercase())
                ?: marshallerRegistry.all().firstOrNull()
        }
        val sessionMarshaller = marshallerRegistry.byFormatId(sessionDefault.lowercase())
        if (sessionMarshaller != null && acceptMatches(acceptHeader, sessionMarshaller)) {
            return sessionMarshaller
        }
        return marshallerRegistry.all().firstOrNull { acceptMatches(acceptHeader, it) }
    }

    private fun acceptMatches(acceptHeader: String, marshaller: ResultMarshaller): Boolean {
        val tokens = acceptHeader.split(",").map { it.trim().substringBefore(";").trim().lowercase() }
        for (t in tokens) {
            if (marshaller.acceptedMimeTypes.any { it.equals(t, ignoreCase = true) }) {
                return true
            }
        }
        return false
    }

    private fun metadataBody(meta: SessionMetadata): Map<String, Any?> =
        linkedMapOf(
            "executionId" to meta.executionId,
            "epoch" to meta.epoch,
            "totalResult" to meta.totalResult,
            "defaultFormat" to meta.defaultFormat,
        )

    private fun createResponseJson(created: CreateSessionResult): Map<String, Any?> {
        val root = linkedMapOf<String, Any?>(
            "executionId" to created.executionId,
            "epoch" to created.epoch,
            "metadata" to metadataBody(created.metadata),
        )
        val fp = created.firstPage
        if (fp != null) {
            val marshaller = marshallerRegistry.byFormatId(
                created.metadata.defaultFormat ?: QueryFormats.ROWS_OBJECTS,
            )!!
            root["firstPage"] = rowsEnvelope(fp, marshaller)
        }
        return root
    }

    private fun rowsEnvelope(page: PagedQueryPayload, marshaller: ResultMarshaller): Map<String, Any?> {
        val data = jsonMapper.readTree(page.body)
        return linkedMapOf(
            "epoch" to page.epoch,
            "pageIndex" to page.pageIndex,
            "pageSize" to page.pageSize,
            "rowCount" to page.rowCount,
            "totalResult" to page.totalResult,
            "hasNext" to page.hasNext,
            "hasPrevious" to page.hasPrevious,
            "data" to data,
        )
    }
}

/**
 * JSON body for [QueryResultRestController.create].
 *
 * @property sql SQL text to execute
 * @property defaultFormat optional default marshaller id for later paging
 * @property includeFirstPage when true, include the first `/rows` envelope under `firstPage`
 * @property firstPageSize presentation page size for the optional first page
 */
data class CreateQueryRequest(
    val sql: String,
    val defaultFormat: String? = null,
    val includeFirstPage: Boolean = false,
    val firstPageSize: Int = 50,
)

/**
 * JSON body for [QueryResultRestController.replace].
 *
 * @property sql replacement SQL text
 * @property defaultFormat optional new default marshaller id
 */
data class ReplaceQueryRequest(
    val sql: String,
    val defaultFormat: String? = null,
)
