package io.qpointz.mill.metadata.api

import io.qpointz.mill.UrnSlug
import io.qpointz.mill.data.schema.SchemaEntityTypeUrns
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.service.FacetTypeManagementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

/**
 * REST controller for facet type catalog management.
 *
 * Provides CRUD operations over the [FacetCatalog]. `{typeKey}` path variables are normalised
 * via [MetadataUrns.normaliseFacetTypePath], accepting prefixed slugs (e.g. `descriptive`),
 * legacy short keys, or full URNs.
 *
 * The `targetType` query parameter follows the same normalisation: pass a prefixed slug
 * (e.g. `table`) or full URN (`urn:mill/metadata/entity-type:table`).
 */
@Tag(name = "metadata-facets", description = "Facet type catalog management endpoints")
@RestController
@RequestMapping("/api/v1/metadata/facets")
class MetadataFacetController(private val service: FacetTypeManagementService) {

    /**
     * Lists all registered facet type descriptors, with optional filtering.
     *
     * @param targetType  optional entity-type URN or slug to filter applicable types;
     *                    e.g. `table` or `urn:mill/metadata/entity-type:table`
     * @param enabledOnly if `true`, only enabled facet types are returned; defaults to `false`
     * @return list of matching [FacetTypeManifest] instances
     */
    @Operation(
        summary = "List facet type descriptors",
        description = "Returns all registered facet types. Filter by targetType (entity-type URN or slug) " +
            "and/or enabledOnly to narrow results."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Facet types returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = FacetTypeManifest::class)))])
    )
    @GetMapping
    fun listFacetTypes(
        @Parameter(description = "Filter by entity-type URN or slug, e.g. table")
        @RequestParam(required = false) targetType: String?,
        @Parameter(description = "If true, return only enabled facet types")
        @RequestParam(required = false, defaultValue = "false") enabledOnly: Boolean
    ): ResponseEntity<List<FacetTypeManifest>> {
        val normType = targetType?.let { UrnSlug.normalise(it, SchemaEntityTypeUrns.PREFIX) }
        return ResponseEntity.ok(service.list(normType, enabledOnly))
    }

    /**
     * Returns a single facet type descriptor by its type key.
     *
     * @param typeKey facet type as a prefixed slug (e.g. `descriptive`), legacy short key,
     *                or full URN
     * @return 200 with the descriptor, or 404 if not found
     */
    @Operation(
        summary = "Get facet type by key",
        description = "Returns the descriptor for the given facet type. typeKey may be a " +
            "prefixed slug (e.g. descriptive), legacy short key, or a full URN."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Facet type found",
            content = [Content(schema = Schema(implementation = FacetTypeManifest::class))]),
        ApiResponse(responseCode = "404", description = "Facet type not found")
    )
    @GetMapping("/{typeKey}")
    fun getFacetTypeByKey(
        @Parameter(description = "Facet type slug or URN, e.g. descriptive")
        @PathVariable typeKey: String
    ): ResponseEntity<FacetTypeManifest> {
        val normKey = MetadataUrns.normaliseFacetTypePath(typeKey)
        return ResponseEntity.ok(service.get(normKey))
    }

    /**
     * Registers a new custom facet type descriptor.
     *
     * The `typeKey` in the request body is normalised to URN format before registration.
     * Platform facet types (descriptive, structural, relation, concept, value-mapping) cannot
     * be re-registered via this endpoint.
     *
     * @param dto the facet type descriptor to register
     * @return 201 with `Location` header pointing to the new resource
     */
    @Operation(
        summary = "Register a new facet type",
        description = "Creates a new custom facet type. The typeKey in the request body is " +
            "normalised to URN form. Returns 201 with a Location header on success."
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Facet type created",
            headers = [Header(name = "Location", description = "URI of the created resource")]),
        ApiResponse(responseCode = "400", description = "Invalid descriptor"),
        ApiResponse(responseCode = "409", description = "Facet type key already registered")
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun registerFacetType(
        @RequestBody body: String,
        @RequestHeader(value = "Content-Type", required = false) contentType: String?
    ): ResponseEntity<FacetTypeManifest> {
        val manifest = service.create(service.parseJson(body, contentType?.let { MediaType.valueOf(it) }))
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{typeKey}")
            .buildAndExpand(manifest.typeKey)
            .toUri()
        return ResponseEntity.created(location).body(manifest)
    }

    /**
     * Updates an existing facet type descriptor.
     *
     * @param typeKey facet type as a prefixed slug or full URN (used to identify the record)
     * @param dto     the updated descriptor; `typeKey` in the body is overridden by the path value
     * @return 200 with the updated descriptor
     */
    @Operation(
        summary = "Update a facet type",
        description = "Replaces the descriptor for the given facet type key. " +
            "typeKey in the body is overridden by the path variable."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Facet type updated",
            content = [Content(schema = Schema(implementation = FacetTypeManifest::class))]),
        ApiResponse(responseCode = "400", description = "Invalid descriptor"),
        ApiResponse(responseCode = "404", description = "Facet type not found")
    )
    @PutMapping(path = ["/{typeKey}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateFacetType(
        @Parameter(description = "Facet type slug or URN to update")
        @PathVariable typeKey: String,
        @RequestBody body: String,
        @RequestHeader(value = "Content-Type", required = false) contentType: String?
    ): ResponseEntity<FacetTypeManifest> {
        val normKey = MetadataUrns.normaliseFacetTypePath(typeKey)
        val manifest = service.update(normKey, service.parseJson(body, contentType?.let { MediaType.valueOf(it) }))
        return ResponseEntity.ok(manifest)
    }

    /**
     * Deletes a non-mandatory facet type descriptor.
     *
     * @param typeKey facet type as a prefixed slug or full URN
     * @return 204 on success, 409 if the facet type is mandatory
     */
    @Operation(
        summary = "Delete a facet type",
        description = "Removes the facet type descriptor. Returns 409 if the type is mandatory."
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Facet type deleted"),
        ApiResponse(responseCode = "404", description = "Facet type not found"),
        ApiResponse(responseCode = "409", description = "Facet type cannot be deleted due to constraints")
    )
    @DeleteMapping("/{typeKey}")
    fun deleteFacetType(
        @Parameter(description = "Facet type slug or URN to delete")
        @PathVariable typeKey: String
    ): ResponseEntity<Void> {
        val normKey = MetadataUrns.normaliseFacetTypePath(typeKey)
        service.delete(normKey)
        return ResponseEntity.noContent().build()
    }
}
