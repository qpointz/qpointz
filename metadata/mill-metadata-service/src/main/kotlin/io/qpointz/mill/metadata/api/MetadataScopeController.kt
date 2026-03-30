package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.MetadataScopeDto
import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.service.MetadataScopeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * REST controller for metadata scope lifecycle management.
 *
 * Scopes are analogous to branches — named, independent sets of facet data with no
 * platform-defined precedence between them. The global scope always exists and cannot be deleted.
 *
 * Path slugs use the local part of the scope URN after `urn:mill/metadata/scope:`.
 * Examples: `global`, `user:alice`, `team:eng`. Expanded server-side via
 * [MetadataUrns.normaliseScopePath].
 */
@Tag(name = "metadata-scopes", description = "Metadata scope lifecycle endpoints")
@RestController
@RequestMapping("/api/v1/metadata/scopes")
class MetadataScopeController(private val scopeService: MetadataScopeService) {

    /**
     * Lists all registered metadata scopes.
     *
     * @return list of all scopes with full URN [MetadataScopeDto.scopeUrn] values
     */
    @Operation(
        summary = "List all metadata scopes",
        description = "Returns all registered scopes. The scopeUrn field in each entry is a full Mill scope URN."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Scopes returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = MetadataScopeDto::class)))])
    )
    @GetMapping
    fun listScopes(): ResponseEntity<List<MetadataScopeDto>> =
        ResponseEntity.ok(scopeService.findAll().map { toDto(it) })

    /**
     * Creates a new metadata scope.
     *
     * The [MetadataScopeDto.scopeUrn] in the request body must be a full Mill scope URN
     * (e.g. `urn:mill/metadata/scope:team:eng`). The `Location` header in the 201 response
     * contains the slug path for the new scope.
     *
     * @param dto the scope definition to create
     * @return 201 with the created scope and `Location` header, or 409 if scope already exists
     */
    @Operation(
        summary = "Create a new metadata scope",
        description = "Creates a scope with the given URN key. Returns 409 if the scope already exists."
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Scope created",
            content = [Content(schema = Schema(implementation = MetadataScopeDto::class))]),
        ApiResponse(responseCode = "409", description = "Scope already exists")
    )
    @PostMapping
    fun createScope(@RequestBody dto: MetadataScopeDto): ResponseEntity<MetadataScopeDto> {
        val created = scopeService.create(dto.scopeUrn, dto.displayName, dto.ownerId)
        val slug = created.res.removePrefix(MetadataUrns.SCOPE_PREFIX)
        val location = URI.create("/api/v1/metadata/scopes/$slug")
        return ResponseEntity.created(location).body(toDto(created))
    }

    /**
     * Deletes the scope identified by the given slug.
     *
     * The `{scopeSlug}` path variable is the local part of the scope URN after
     * `urn:mill/metadata/scope:`, e.g. `user:alice` or `team:eng`. Attempting to delete
     * the global scope (`global`) returns 409.
     *
     * @param scopeSlug the local scope slug or full URN path segment
     * @return 204 on success, 409 if the scope is the global scope or is otherwise protected
     */
    @Operation(
        summary = "Delete a metadata scope",
        description = "Deletes the scope identified by scopeSlug. " +
            "Returns 409 if attempting to delete the global scope."
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Scope deleted"),
        ApiResponse(responseCode = "409", description = "Cannot delete the global scope")
    )
    @DeleteMapping("/{scopeSlug}")
    fun deleteScope(
        @Parameter(description = "Local scope slug, e.g. user:alice or team:eng")
        @PathVariable scopeSlug: String
    ): ResponseEntity<Void> {
        val scopeId = MetadataUrns.normaliseScopePath(scopeSlug)
        scopeService.delete(scopeId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Converts a domain [MetadataScope] to its [MetadataScopeDto] REST representation.
     *
     * @param scope the domain scope to convert
     * @return the corresponding DTO
     */
    private fun toDto(scope: MetadataScope): MetadataScopeDto =
        MetadataScopeDto(
            scopeUrn = scope.res,
            displayName = scope.displayName,
            ownerId = scope.ownerId,
            createdAt = scope.createdAt
        )
}
