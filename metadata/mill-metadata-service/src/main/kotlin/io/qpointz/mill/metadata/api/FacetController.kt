package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.FacetDto
import io.qpointz.mill.metadata.service.MetadataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Endpoints for resolved and scope-specific facet retrieval. */
@Tag(name = "Facets", description = "Facet retrieval endpoints")
@RestController
@RequestMapping("/api/metadata/v1/facets")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:8080"])
class FacetController(private val metadataService: MetadataService) {

    /** Returns merged facet for requested principal context. */
    @Operation(summary = "Get merged facet for principal context")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Facet found"),
            ApiResponse(responseCode = "404", description = "Entity or facet not found")
        ]
    )
    @GetMapping("/entities/{entityId}/types/{facetType}")
    fun getMergedFacet(
        @PathVariable entityId: String,
        @PathVariable facetType: String,
        @RequestParam(name = "userId", required = false) userId: String?,
        @RequestParam(name = "teams", required = false) teams: List<String>?,
        @RequestParam(name = "roles", required = false) roles: List<String>?
    ): ResponseEntity<FacetDto> =
        metadataService.findById(entityId)
            .flatMap { entity ->
                val mergedData = entity.getMergedFacet(
                    facetType,
                    userId ?: "anonymous",
                    teams ?: emptyList(),
                    roles ?: emptyList(),
                    Any::class.java
                )
                val scopes = entity.getFacetScopes(facetType)
                mergedData.map { FacetDto(facetType = facetType, data = it, availableScopes = scopes) }
            }
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    /** Returns facet payload for an explicit scope key. */
    @Operation(summary = "Get facet by explicit scope")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Facet found"),
            ApiResponse(responseCode = "404", description = "Entity or facet not found")
        ]
    )
    @GetMapping("/entities/{entityId}/types/{facetType}/scopes/{scope}")
    fun getFacetByScope(
        @PathVariable entityId: String,
        @PathVariable facetType: String,
        @PathVariable scope: String
    ): ResponseEntity<FacetDto> =
        metadataService.findById(entityId)
            .flatMap { entity ->
                val facetData = entity.getFacet(facetType, scope, Any::class.java)
                val scopes = entity.getFacetScopes(facetType)
                facetData.map { FacetDto(facetType = facetType, data = it, availableScopes = scopes) }
            }
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
}
