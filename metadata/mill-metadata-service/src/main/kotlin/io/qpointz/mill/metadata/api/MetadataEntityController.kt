package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.FacetResponseDto
import io.qpointz.mill.metadata.api.dto.MetadataEntityDto
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.metadata.service.MetadataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Read-only REST controller for metadata entity discovery and facet resolution.
 *
 * Facet type keys in path variables are normalised via [MetadataUrns.normaliseFacetTypePath],
 * accepting prefixed slugs (e.g. `descriptive`), legacy short keys, or full URNs.
 *
 * The `context` query parameter is a comma-separated list of scope prefixed slugs
 * (e.g. `global`, `global,user:alice`). Omitting `context` defaults to the global scope.
 */
@Tag(name = "metadata-entities", description = "Read-only metadata entity and facet resolution endpoints")
@RestController
@RequestMapping("/api/v1/metadata/entities")
class MetadataEntityController(private val metadataService: MetadataService) {

    /**
     * Lists metadata entities with optional filtering by schema and/or table coordinates.
     *
     * @param schema  optional schema name filter
     * @param table   optional table name filter (requires [schema])
     * @param context comma-separated scope slugs for facet resolution; defaults to global
     * @return list of matching entities with scope-resolved facets
     */
    @Operation(
        summary = "List metadata entities",
        description = "Returns all entities, optionally filtered by schema and table. " +
            "Facets are included as stored; use the /facets endpoint for scope-merged views."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Entities returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = MetadataEntityDto::class)))])
    )
    @GetMapping
    fun listEntities(
        @Parameter(description = "Filter by schema name") @RequestParam(required = false) schema: String?,
        @Parameter(description = "Filter by table name") @RequestParam(required = false) table: String?,
        @Parameter(description = "Comma-separated scope slugs, e.g. global,user:alice")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<List<MetadataEntityDto>> {
        val ctx = MetadataContext.parse(context)
        var entities = metadataService.findAll()
        if (schema != null) entities = entities.filter { it.schemaName == schema }
        if (table != null) entities = entities.filter { it.tableName == table }
        val dtos = entities.map { toDto(it, ctx) }
        return ResponseEntity.ok(dtos)
    }

    /**
     * Returns a single entity by its identifier.
     *
     * @param id      entity identifier string
     * @param context comma-separated scope slugs; defaults to global
     * @return 200 with the entity DTO, or 404 if not found
     */
    @Operation(
        summary = "Get entity by id",
        description = "Returns the entity with the given identifier. " +
            "The context parameter controls which scopes are included in the response."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Entity found",
            content = [Content(schema = Schema(implementation = MetadataEntityDto::class))]),
        ApiResponse(responseCode = "404", description = "Entity not found")
    )
    @GetMapping("/{id}")
    fun getEntityById(
        @Parameter(description = "Entity identifier") @PathVariable id: String,
        @Parameter(description = "Comma-separated scope slugs")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<MetadataEntityDto> {
        val ctx = MetadataContext.parse(context)
        return metadataService.findById(id)
            .map { ResponseEntity.ok(toDto(it, ctx)) }
            .orElse(ResponseEntity.notFound().build())
    }

    /**
     * Returns all facets of an entity merged across the requested context.
     *
     * For each facet type present on the entity, the last matching scope in the context
     * wins. Facet types with no data under any scope in the context are omitted from the result.
     *
     * @param id      entity identifier string
     * @param context comma-separated scope slugs; defaults to global
     * @return map of facet type URN → [FacetResponseDto], or 404 if entity not found
     */
    @Operation(
        summary = "Get all merged facets for an entity",
        description = "Returns one entry per facet type that has data under any scope in the " +
            "requested context. Keys are full facet type URN strings. " +
            "The last matching scope in the context wins."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Facets returned"),
        ApiResponse(responseCode = "404", description = "Entity not found")
    )
    @GetMapping("/{id}/facets")
    fun getEntityFacets(
        @Parameter(description = "Entity identifier") @PathVariable id: String,
        @Parameter(description = "Comma-separated scope slugs")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<Map<String, FacetResponseDto>> {
        val ctx = MetadataContext.parse(context)
        val entityOpt = metadataService.findById(id)
        if (entityOpt.isEmpty) return ResponseEntity.notFound().build()
        val entity = entityOpt.get()
        val result = mutableMapOf<String, FacetResponseDto>()
        for (facetType in entity.facets.keys) {
            val payload = resolveForContext(entity.facets[facetType] ?: continue, ctx)
            if (payload != null) {
                result[facetType] = FacetResponseDto(facetType = facetType, payload = payload)
            }
        }
        return ResponseEntity.ok(result)
    }

    /**
     * Returns a single facet type's merged payload for an entity.
     *
     * @param id      entity identifier string
     * @param typeKey facet type as a prefixed slug (e.g. `descriptive`) or full URN
     * @param context comma-separated scope slugs; defaults to global
     * @return [FacetResponseDto] with the merged payload, or 404 if entity or facet not found
     */
    @Operation(
        summary = "Get merged facet by type for an entity",
        description = "Returns the merged payload for the specified facet type using the " +
            "given context. typeKey may be a prefixed slug (e.g. descriptive) or a full URN."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Facet returned",
            content = [Content(schema = Schema(implementation = FacetResponseDto::class))]),
        ApiResponse(responseCode = "404", description = "Entity or facet type not found")
    )
    @GetMapping("/{id}/facets/{typeKey}")
    fun getEntityFacetByType(
        @Parameter(description = "Entity identifier") @PathVariable id: String,
        @Parameter(description = "Facet type slug or URN, e.g. descriptive")
        @PathVariable typeKey: String,
        @Parameter(description = "Comma-separated scope slugs")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<FacetResponseDto> {
        val ctx = MetadataContext.parse(context)
        val normType = MetadataUrns.normaliseFacetTypePath(typeKey)
        val entityOpt = metadataService.findById(id)
        if (entityOpt.isEmpty) return ResponseEntity.notFound().build()
        val entity = entityOpt.get()
        val scopeMap = entity.facets[normType] ?: return ResponseEntity.notFound().build()
        val payload = resolveForContext(scopeMap, ctx) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(FacetResponseDto(facetType = normType, payload = payload))
    }

    /**
     * Converts a domain entity to its REST DTO representation.
     *
     * @param entity the domain entity to convert
     * @param ctx    the scope context; unused in this read-only view (full facets are returned)
     * @return [MetadataEntityDto] with all facets
     */
    private fun toDto(
        entity: io.qpointz.mill.metadata.domain.MetadataEntity,
        @Suppress("UNUSED_PARAMETER") ctx: MetadataContext
    ): MetadataEntityDto = MetadataEntityDto(
        id = entity.id,
        type = entity.type,
        schemaName = entity.schemaName,
        tableName = entity.tableName,
        attributeName = entity.attributeName,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        createdBy = entity.createdBy,
        updatedBy = entity.updatedBy,
        facets = entity.facets.mapValues { (_, v) -> v as Any? }
    )

    /**
     * Resolves a scope map using the context's ordered scope list (last wins).
     *
     * @param scopeMap the map of scope URN → payload for a single facet type
     * @param ctx      the scope context to apply
     * @return the resolved payload, or `null` if no scope in the context has data
     */
    private fun resolveForContext(scopeMap: Map<String, Any?>, ctx: MetadataContext): Any? {
        var result: Any? = null
        for (scope in ctx.scopes) {
            scopeMap[scope]?.let { result = it }
        }
        return result
    }
}
