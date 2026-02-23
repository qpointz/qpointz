package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.FacetDto
import io.qpointz.mill.metadata.api.dto.MetadataEntityDto
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

/** Read-only metadata endpoints for entity and facet discovery. */
@Tag(name = "Metadata", description = "Read-only metadata entity and facet endpoints")
@RestController
@RequestMapping("/api/metadata/v1")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:8080"])
class MetadataController(private val metadataService: MetadataService, private val dtoMapper: DtoMapper) {

    /** Returns entity by id with facets resolved for selected scope. */
    @Operation(summary = "Get entity by id")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Entity found"),
            ApiResponse(responseCode = "404", description = "Entity not found")
        ]
    )
    @GetMapping("/entities/{id}")
    fun getEntityById(
        @PathVariable id: String,
        @RequestParam(required = false, defaultValue = "global") scope: String
    ): ResponseEntity<MetadataEntityDto> =
        metadataService.findById(id)
            .map { dtoMapper.toDto(it, scope) }
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    /** Returns table-level entity by schema/table coordinates. */
    @Operation(summary = "Get table entity by schema and table")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Entity found"),
            ApiResponse(responseCode = "404", description = "Entity not found")
        ]
    )
    @GetMapping("/schemas/{schema}/tables/{table}")
    fun getTable(
        @PathVariable schema: String,
        @PathVariable table: String,
        @RequestParam(required = false, defaultValue = "global") scope: String
    ): ResponseEntity<MetadataEntityDto> =
        metadataService.findByLocation(schema, table, null)
            .map { dtoMapper.toDto(it, scope) }
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    /** Returns attribute-level entity by schema/table/attribute coordinates. */
    @Operation(summary = "Get attribute entity by schema, table, and attribute")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Entity found"),
            ApiResponse(responseCode = "404", description = "Entity not found")
        ]
    )
    @GetMapping("/schemas/{schema}/tables/{table}/attributes/{attribute}")
    fun getAttribute(
        @PathVariable schema: String,
        @PathVariable table: String,
        @PathVariable attribute: String,
        @RequestParam(required = false, defaultValue = "global") scope: String
    ): ResponseEntity<MetadataEntityDto> =
        metadataService.findByLocation(schema, table, attribute)
            .map { dtoMapper.toDto(it, scope) }
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    /** Returns all entities (type filter reserved for backward compatibility). */
    @Operation(summary = "List metadata entities")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Entities returned")])
    @GetMapping("/entities")
    fun getEntities(
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false, defaultValue = "global") scope: String
    ): ResponseEntity<List<MetadataEntityDto>> {
        val entities = metadataService.findAll().map { dtoMapper.toDto(it, scope) }
        return ResponseEntity.ok(entities)
    }

    /** Returns facet payload for entity/type/scope. */
    @Operation(summary = "Get entity facet for type and scope")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Facet found"),
            ApiResponse(responseCode = "404", description = "Entity or facet not found")
        ]
    )
    @GetMapping("/entities/{id}/facets/{facetType}")
    fun getFacet(
        @PathVariable id: String,
        @PathVariable facetType: String,
        @RequestParam(required = false, defaultValue = "global") scope: String
    ): ResponseEntity<FacetDto> =
        metadataService.findById(id)
            .flatMap { entity ->
                val facetData = entity.getFacet(facetType, scope, Any::class.java)
                val scopes = entity.getFacetScopes(facetType)
                facetData.map { FacetDto(facetType = facetType, data = it, availableScopes = scopes) }
            }
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    /** Returns available scopes for a facet type on an entity. */
    @Operation(summary = "List available facet scopes for entity and facet type")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Scopes returned"),
            ApiResponse(responseCode = "404", description = "Entity not found")
        ]
    )
    @GetMapping("/entities/{id}/facets/{facetType}/scopes")
    fun getFacetScopes(@PathVariable id: String, @PathVariable facetType: String): ResponseEntity<Set<String>> =
        metadataService.findById(id)
            .map { it.getFacetScopes(facetType) }
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    /** Returns entities connected through relation/concept facets. */
    @Operation(summary = "Get entities related to given entity")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Related entities returned"),
            ApiResponse(responseCode = "404", description = "Entity not found")
        ]
    )
    @GetMapping("/entities/{id}/related")
    fun getRelatedEntities(
        @PathVariable id: String,
        @RequestParam(required = false, defaultValue = "global") scope: String
    ): ResponseEntity<List<MetadataEntityDto>> =
        metadataService.findById(id)
            .map {
                val related = metadataService.findRelatedEntities(id, scope).map { e -> dtoMapper.toDto(e, scope) }
                ResponseEntity.ok(related)
            }
            .orElse(ResponseEntity.notFound().build())
}
