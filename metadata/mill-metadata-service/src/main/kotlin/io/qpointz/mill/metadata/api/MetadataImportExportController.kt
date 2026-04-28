package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.ImportResultDto
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataExportFormat
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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller for bulk metadata import and export operations.
 *
 * Import accepts a YAML file upload (multipart/form-data) as a `kind:`-discriminated
 * multi-document stream (optional `---` separators). Legacy `entities:` list envelopes are
 * rejected by the server parser.
 *
 * Export returns all persisted scopes and catalog facet-type definitions, plus every entity
 * with facet rows filtered by the `scope` query (facet rows only).
 */
@Tag(name = "metadata-import-export", description = "Bulk metadata import and export endpoints")
@RestController
@RequestMapping("/api/v1/metadata")
class MetadataImportExportController(private val importService: MetadataImportService) {

    /**
     * Imports metadata from a canonical multi-document YAML upload.
     *
     * Documents use `kind:` values `MetadataScope`, `FacetTypeDefinition`, and `MetadataEntity`
     * (see SPEC §15). [ImportMode.REPLACE] deletes all entities before importing; [ImportMode.MERGE]
     * (default) preserves entities not mentioned in the file.
     *
     * @param file   the YAML file to import as a multipart part named `file`
     * @param mode   import mode; `MERGE` (default) or `REPLACE`
     * @param actor  identity of the importing actor; recorded in audit entries; defaults to `system`
     * @return summary of the import operation
     */
    @Operation(
        summary = "Import metadata from YAML",
        description = "Multipart upload (`file` part) of `kind:`-discriminated YAML documents " +
            "(optional `---`). Legacy `entities:` list envelopes are not supported. " +
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
     * Exports persisted metadata as canonical YAML or JSON.
     *
     * The `scope` query filters **facet assignment rows** embedded under entities only (scopes and
     * facet type definition documents are always included in full). Omitted or blank `scope` limits
     * facets to the global scope; `all` or `*` exports every facet row; comma-separated values form
     * a union.
     *
     * The `format` query selects the representation (`yaml` default, `json`). When `format` is
     * omitted, `Accept: application/json` selects JSON; an explicit `format` wins over `Accept`.
     *
     * @param scope  optional facet scope filter (see above)
     * @param format export representation: `yaml` or `json`
     * @param accept optional `Accept` header used when `format` is omitted
     * @return YAML (`text/yaml`) or JSON (`application/json`) body
     */
    @Operation(
        summary = "Export metadata (YAML or JSON)",
        description = "Canonical export: all MetadataScope rows, catalog FacetTypeDefinition rows, " +
            "then MetadataEntity documents with facets filtered by `scope`. " +
            "`format=yaml|json` (default yaml); explicit format overrides Accept. " +
            "`scope` omitted → global facets only; `all` or `*` → all facet rows; comma-separated → union."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Export produced",
            content = [
                Content(mediaType = "text/yaml"),
                Content(mediaType = "application/json")
            ]
        ),
        ApiResponse(responseCode = "400", description = "Invalid scope or format token")
    )
    @GetMapping("/export", produces = ["text/yaml", "application/json"])
    fun exportMetadata(
        @Parameter(
            description = "Facet scope filter: omit for global only; `all` or `*` for all rows; " +
                "comma-separated URNs/slugs for union"
        )
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Export format: yaml (default) or json")
        @RequestParam(required = false) format: String?,
        @Parameter(description = "Used when format is omitted: application/json requests JSON")
        @RequestHeader(value = HttpHeaders.ACCEPT, required = false) accept: String?
    ): ResponseEntity<String> {
        val exportFormat = resolveExportFormat(format, accept)
        val body = importService.export(scope, exportFormat)
        return when (exportFormat) {
            MetadataExportFormat.YAML -> ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"metadata-export.yaml\""
                )
                .contentType(MediaType.parseMediaType("text/yaml"))
                .body(body)
            MetadataExportFormat.JSON -> ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"metadata-export.json\""
                )
                .contentType(MediaType.parseMediaType("application/json"))
                .body(body)
        }
    }

    private fun resolveExportFormat(format: String?, accept: String?): MetadataExportFormat {
        val trimmed = format?.trim().orEmpty()
        if (trimmed.isNotEmpty()) {
            return when (trimmed.lowercase()) {
                "yaml" -> MetadataExportFormat.YAML
                "json" -> MetadataExportFormat.JSON
                else -> throw MillStatuses.badRequestRuntime("format must be yaml or json")
            }
        }
        val acceptVal = accept?.lowercase().orEmpty()
        return if (acceptVal.contains("application/json")) {
            MetadataExportFormat.JSON
        } else {
            MetadataExportFormat.YAML
        }
    }
}
