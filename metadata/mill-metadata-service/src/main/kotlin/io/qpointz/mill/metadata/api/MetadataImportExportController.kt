package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.ImportResultDto
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.service.MetadataImportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller for bulk metadata import and export operations.
 *
 * Import accepts a YAML file upload (multipart/form-data) in the extended format supporting
 * both `entities:` and optional `facet-types:` sections. Multi-document YAML separated by
 * `---` is also supported.
 *
 * Export produces a YAML document in the same format, filtered to the requested scope.
 */
@Tag(name = "metadata-import-export", description = "Bulk metadata import and export endpoints")
@RestController
@RequestMapping("/api/v1/metadata")
class MetadataImportExportController(private val importService: MetadataImportService) {

    /**
     * Imports metadata entities and optional custom facet type definitions from a YAML file.
     *
     * The uploaded YAML may contain an `entities:` section and an optional `facet-types:` section.
     * Multiple YAML documents separated by `---` are supported within a single file.
     *
     * Legacy short-form facet type keys (`descriptive`, `global`) are normalised to URN notation
     * during import. Use [ImportMode.REPLACE] to clear all existing entities before importing;
     * [ImportMode.MERGE] (default) preserves entities not mentioned in the file.
     *
     * @param file   the YAML file to import as a multipart part named `file`
     * @param mode   import mode; `MERGE` (default) or `REPLACE`
     * @param actor  identity of the importing actor; recorded in audit entries; defaults to `system`
     * @return summary of the import operation
     */
    @Operation(
        summary = "Import metadata from YAML",
        description = "Accepts a multipart/form-data upload with the YAML file in the `file` part. " +
            "Supports entities: and facet-types: sections. Multi-document YAML is accepted. " +
            "mode=REPLACE deletes all entities before importing; default is MERGE."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Import completed (see errors[] for non-fatal issues)",
            content = [Content(schema = Schema(implementation = ImportResultDto::class))]),
        ApiResponse(responseCode = "400", description = "Malformed YAML or missing file part")
    )
    @PostMapping("/import", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun importMetadata(
        @Parameter(description = "YAML file to import") @RequestParam("file") file: MultipartFile,
        @Parameter(description = "Import mode: MERGE (default) or REPLACE")
        @RequestParam(required = false, defaultValue = "MERGE") mode: ImportMode,
        @Parameter(description = "Actor identity for audit log; defaults to system")
        @RequestParam(required = false, defaultValue = "system") actor: String
    ): ResponseEntity<ImportResultDto> {
        val result = importService.import(file.inputStream, mode, actor)
        val dto = ImportResultDto(
            entitiesImported = result.entitiesImported,
            facetTypesImported = result.facetTypesImported,
            errors = result.errors
        )
        return ResponseEntity.ok(dto)
    }

    /**
     * Exports all metadata entities as a YAML document, filtered to the requested scope.
     *
     * Custom facet type descriptors (non-platform) are included as a `facet-types:` section
     * preceding the `entities:` section. All keys are exported in full URN notation.
     *
     * @param scope the scope key (full URN or prefixed slug) to filter exported facets;
     *              defaults to `urn:mill/metadata/scope:global`
     * @return YAML document as `text/yaml`
     */
    @Operation(
        summary = "Export metadata as YAML",
        description = "Returns all entities with facets filtered to the requested scope. " +
            "Custom facet types are included in a facet-types: preamble section. " +
            "All keys use full URN notation."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "YAML export produced",
            content = [Content(mediaType = "text/yaml")])
    )
    @GetMapping("/export", produces = ["text/yaml"])
    fun exportMetadata(
        @Parameter(description = "Scope key (full URN or slug, e.g. global); defaults to global scope")
        @RequestParam(required = false) scope: String?
    ): ResponseEntity<String> {
        val normScope = if (scope != null) MetadataUrns.normaliseScopePath(scope)
                        else MetadataUrns.SCOPE_GLOBAL
        val yaml = importService.export(normScope)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"metadata-export.yaml\"")
            .contentType(MediaType.parseMediaType("text/yaml"))
            .body(yaml)
    }
}
