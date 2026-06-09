package io.qpointz.mill.analysis.queries.web

import io.qpointz.mill.analysis.queries.SavedQuery
import io.qpointz.mill.analysis.queries.SavedQueryCatalog
import io.qpointz.mill.analysis.queries.web.dto.SavedQueryCreateRequest
import io.qpointz.mill.analysis.queries.web.dto.SavedQueryListResponse
import io.qpointz.mill.analysis.queries.web.dto.SavedQueryWireDto
import io.qpointz.mill.analysis.queries.web.dto.SavedQueryWriteRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

/**
 * Saved-query catalog for mill-ui Analysis ({@code /api/v1/analysis/queries}).
 *
 * @param catalog persistence port for saved queries
 */
@RestController
@RequestMapping("/api/v1/analysis/queries")
@Tag(name = "analysis", description = "Analysis view configuration and catalog")
class SavedQueriesRestController(
    private val catalog: SavedQueryCatalog,
) {

    /**
     * @return all saved queries
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "List saved queries")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Catalog returned (possibly empty)"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun list(): SavedQueryListResponse =
        SavedQueryListResponse(
            queries = catalog.findAll().map { SavedQueryWireMapper.toWire(it) },
        )

    /**
     * @param queryId catalog identifier
     * @return a single saved query
     */
    @GetMapping("/{queryId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get saved query by id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Query returned"),
        ApiResponse(responseCode = "404", description = "Unknown query id"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun getById(@PathVariable queryId: String): SavedQueryWireDto {
        val query = catalog.findById(queryId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Saved query not found: $queryId")
        return SavedQueryWireMapper.toWire(query)
    }

    /**
     * @param request create payload with optional client-supplied id
     * @return the persisted query
     */
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create saved query")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Query created"),
        ApiResponse(responseCode = "409", description = "Id already exists"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun create(@Valid @RequestBody request: SavedQueryCreateRequest): ResponseEntity<SavedQueryWireDto> {
        val id = request.id?.trim()?.takeIf { it.isNotEmpty() }
            ?: SavedQueryIdGenerator.generate(request.name, catalog.findAll().map { it.id }.toSet())
        if (catalog.findById(id) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Saved query already exists: $id")
        }
        val now = Instant.now()
        val saved = catalog.save(
            SavedQuery(
                id = id,
                name = request.name.trim(),
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
                sql = request.sql,
                createdAt = now,
                updatedAt = now,
                tags = request.tags.orEmpty(),
            ),
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(SavedQueryWireMapper.toWire(saved))
    }

    /**
     * @param queryId catalog identifier
     * @param request update payload
     * @return the updated query
     */
    @PutMapping(
        path = ["/{queryId}"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Operation(summary = "Update saved query")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Query updated"),
        ApiResponse(responseCode = "404", description = "Unknown query id"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun update(
        @PathVariable queryId: String,
        @Valid @RequestBody request: SavedQueryWriteRequest,
    ): SavedQueryWireDto {
        val existing = catalog.findById(queryId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Saved query not found: $queryId")
        val updated = catalog.save(
            existing.copy(
                name = request.name.trim(),
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
                sql = request.sql,
                updatedAt = Instant.now(),
                tags = request.tags.orEmpty(),
            ),
        )
        return SavedQueryWireMapper.toWire(updated)
    }

    /**
     * @param queryId catalog identifier
     */
    @DeleteMapping("/{queryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete saved query")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Query deleted"),
        ApiResponse(responseCode = "404", description = "Unknown query id"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun delete(@PathVariable queryId: String) {
        if (!catalog.deleteById(queryId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Saved query not found: $queryId")
        }
    }
}
