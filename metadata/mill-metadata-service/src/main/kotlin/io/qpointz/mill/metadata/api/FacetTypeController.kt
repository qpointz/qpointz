package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataTargetType
import io.qpointz.mill.metadata.service.FacetCatalog
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/** CRUD API for facet type descriptors managed by [FacetCatalog]. */
@Tag(name = "Facet Types", description = "Facet type descriptor management endpoints")
@RestController
@RequestMapping("/api/metadata/v1/facet-types")
@ConditionalOnBean(FacetCatalog::class)
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:8080"])
class FacetTypeController(private val facetCatalog: FacetCatalog) {

    /** Lists descriptors with optional target-type and enabled filters. */
    @Operation(summary = "List facet type descriptors")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Facet types returned")])
    @GetMapping
    fun listAll(
        @RequestParam(name = "targetType", required = false) targetType: MetadataTargetType?,
        @RequestParam(name = "enabledOnly", required = false, defaultValue = "false") enabledOnly: Boolean
    ): Collection<FacetTypeDescriptor> {
        if (targetType != null) return facetCatalog.getForTargetType(targetType)
        return if (enabledOnly) facetCatalog.getEnabled() else facetCatalog.getAll()
    }

    /** Returns descriptor by type key. */
    @Operation(summary = "Get facet type descriptor by key")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Facet type found"),
            ApiResponse(responseCode = "404", description = "Facet type not found")
        ]
    )
    @GetMapping("/{typeKey}")
    fun getByTypeKey(@PathVariable typeKey: String): ResponseEntity<FacetTypeDescriptor> =
        facetCatalog.get(typeKey)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    /** Creates new facet type descriptor. */
    @Operation(summary = "Create facet type descriptor")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Facet type created"),
            ApiResponse(responseCode = "400", description = "Invalid facet type descriptor")
        ]
    )
    @PostMapping
    fun create(@RequestBody descriptor: FacetTypeDescriptor): ResponseEntity<FacetTypeDescriptor> =
        try {
            descriptor.createdAt = Instant.now()
            descriptor.updatedAt = Instant.now()
            facetCatalog.register(descriptor)
            ResponseEntity.ok(descriptor)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }

    /** Updates existing descriptor identified by path type key. */
    @Operation(summary = "Update facet type descriptor")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Facet type updated"),
            ApiResponse(responseCode = "400", description = "Invalid update request")
        ]
    )
    @PutMapping("/{typeKey}")
    fun update(
        @PathVariable typeKey: String,
        @RequestBody descriptor: FacetTypeDescriptor
    ): ResponseEntity<FacetTypeDescriptor> =
        try {
            descriptor.typeKey = typeKey
            descriptor.updatedAt = Instant.now()
            facetCatalog.update(descriptor)
            ResponseEntity.ok(descriptor)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }

    /** Deletes non-mandatory descriptor by type key. */
    @Operation(summary = "Delete facet type descriptor")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Facet type deleted"),
            ApiResponse(responseCode = "400", description = "Facet type cannot be deleted")
        ]
    )
    @DeleteMapping("/{typeKey}")
    fun delete(@PathVariable typeKey: String): ResponseEntity<Void> =
        try {
            facetCatalog.delete(typeKey)
            ResponseEntity.noContent().build()
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
}
