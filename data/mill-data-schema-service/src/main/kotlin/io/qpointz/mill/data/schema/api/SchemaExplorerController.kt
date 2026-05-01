package io.qpointz.mill.data.schema.api

import io.qpointz.mill.data.schema.api.dto.ColumnDto
import io.qpointz.mill.data.schema.api.dto.ModelRootDto
import io.qpointz.mill.data.schema.api.dto.SchemaContextDto
import io.qpointz.mill.data.schema.api.dto.SchemaDto
import io.qpointz.mill.data.schema.api.dto.SchemaExplorerTreeDto
import io.qpointz.mill.data.schema.api.dto.SchemaListItemDto
import io.qpointz.mill.data.schema.api.dto.TableDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller exposing schema explorer endpoints under `/api/v1/schema`.
 *
 * Canonical paths use `/api/v1/schema` and `/api/v1/schema/{schemaName}/…`. Legacy `/api/v1/schema/schemas/…`
 * aliases delegate to the same service and are deprecated for removal.
 */
@RestController
@RequestMapping("/api/v1/schema")
@Tag(name = "schema", description = "Browse physical schema and merged metadata facets")
class SchemaExplorerController(
    private val schemaExplorerService: SchemaExplorerService
) {

    /**
     * Returns the currently selected schema context.
     *
     * @return fixed global context payload for phase 1
     */
    @GetMapping("/context")
    @Operation(summary = "Get active schema context")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Current schema context",
                content = [Content(schema = Schema(implementation = SchemaContextDto::class))]
            )
        ]
    )
    fun getContext(): SchemaContextDto = schemaExplorerService.getContext()

    /**
     * Lists schemas with descriptive facets.
     *
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @return schema list payload
     */
    @GetMapping("")
    @Operation(summary = "List schemas")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Schema list",
                content = [Content(array = ArraySchema(schema = Schema(implementation = SchemaListItemDto::class)))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter")
        ]
    )
    fun listSchemas(
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): List<SchemaListItemDto> = schemaExplorerService.listSchemas(scope, context, origin, facetMode)

    /**
     * Legacy list path; identical to [listSchemas]. Prefer `GET /api/v1/schema`.
     *
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @param facetMode facet expansion policy: none | direct | hierarchy
     * @return schema list payload
     */
    @Deprecated("Use GET /api/v1/schema instead. This path will be removed.")
    @GetMapping("/schemas")
    @Operation(
        summary = "List schemas (legacy path)",
        description = "Deprecated: use `GET /api/v1/schema` without `/schemas`.",
        deprecated = true
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Schema list",
                content = [Content(array = ArraySchema(schema = Schema(implementation = SchemaListItemDto::class)))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter")
        ]
    )
    fun listSchemasLegacy(
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): List<SchemaListItemDto> = schemaExplorerService.listSchemas(scope, context, origin, facetMode)

    /**
     * Returns schema tree payload for initial explorer loading.
     *
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @return model root plus schema detail list containing table summaries
     */
    @GetMapping("/tree")
    @Operation(summary = "Get schema tree payload")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Model root and physical schemas",
                content = [Content(schema = Schema(implementation = SchemaExplorerTreeDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter")
        ]
    )
    fun getTree(
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "none") facetMode: String
    ): SchemaExplorerTreeDto = schemaExplorerService.getTree(scope, context, origin, facetMode)

    /**
     * Returns the logical catalog model root (SPEC §3f) with optional facet payloads.
     *
     * @param scope optional comma-separated scopes
     * @param context deprecated alias for [scope] when [scope] is absent
     * @param facetMode facet expansion policy
     * @return model root DTO (stable metadata entity URN)
     */
    @GetMapping("/model")
    @Operation(summary = "Get logical model root")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Model root",
                content = [Content(schema = Schema(implementation = ModelRootDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter")
        ]
    )
    fun getModelRoot(
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): ModelRootDto = schemaExplorerService.getModelRoot(scope, context, origin, facetMode)

    /**
     * Returns one schema detail payload.
     *
     * @param schemaName schema name
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @return schema detail
     */
    @GetMapping("/{schemaName}")
    @Operation(summary = "Get schema detail")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Schema detail payload",
                content = [Content(schema = Schema(implementation = SchemaDto::class))
                ]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter"),
            ApiResponse(responseCode = "404", description = "Schema not found")
        ]
    )
    fun getSchema(
        @PathVariable schemaName: String,
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): SchemaDto = schemaExplorerService.getSchema(schemaName, scope, context, origin, facetMode)

    /**
     * Legacy schema detail path; identical to [getSchema]. Prefer `GET /api/v1/schema/{schemaName}`.
     *
     * @param schemaName schema name
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @param facetMode facet expansion policy: none | direct | hierarchy
     * @return schema detail
     */
    @Deprecated("Use GET /api/v1/schema/{schemaName} instead. This path will be removed.")
    @GetMapping("/schemas/{schemaName}")
    @Operation(
        summary = "Get schema detail (legacy path)",
        description = "Deprecated: use `GET /api/v1/schema/{schemaName}` without the `/schemas` prefix.",
        deprecated = true
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Schema detail payload",
                content = [Content(schema = Schema(implementation = SchemaDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter"),
            ApiResponse(responseCode = "404", description = "Schema not found")
        ]
    )
    fun getSchemaLegacy(
        @PathVariable schemaName: String,
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): SchemaDto = schemaExplorerService.getSchema(schemaName, scope, context, origin, facetMode)

    /**
     * Returns one table detail payload.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @return table detail
     */
    @GetMapping("/{schemaName}/tables/{tableName}")
    @Operation(summary = "Get table detail")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Table detail payload",
                content = [Content(schema = Schema(implementation = TableDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter"),
            ApiResponse(responseCode = "404", description = "Schema or table not found")
        ]
    )
    fun getTable(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): TableDto = schemaExplorerService.getTable(schemaName, tableName, scope, context, origin, facetMode)

    /**
     * Legacy table detail path; identical to [getTable]. Prefer `GET /api/v1/schema/{schemaName}/tables/{tableName}`.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @param facetMode facet expansion policy: none | direct | hierarchy
     * @return table detail
     */
    @Deprecated(
        "Use GET /api/v1/schema/{schemaName}/tables/{tableName} instead. This path will be removed."
    )
    @GetMapping("/schemas/{schemaName}/tables/{tableName}")
    @Operation(
        summary = "Get table detail (legacy path)",
        description = "Deprecated: use `GET /api/v1/schema/{schemaName}/tables/{tableName}` without the `/schemas` prefix.",
        deprecated = true
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Table detail payload",
                content = [Content(schema = Schema(implementation = TableDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter"),
            ApiResponse(responseCode = "404", description = "Schema or table not found")
        ]
    )
    fun getTableLegacy(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): TableDto = schemaExplorerService.getTable(schemaName, tableName, scope, context, origin, facetMode)

    /**
     * Returns one column detail payload.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @return column detail
     */
    @GetMapping("/{schemaName}/tables/{tableName}/columns/{columnName}")
    @Operation(summary = "Get column detail")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Column detail payload",
                content = [Content(schema = Schema(implementation = ColumnDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter"),
            ApiResponse(responseCode = "404", description = "Schema, table, or column not found")
        ]
    )
    fun getColumn(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @PathVariable columnName: String,
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): ColumnDto = schemaExplorerService.getColumn(schemaName, tableName, columnName, scope, context, origin, facetMode)

    /**
     * Legacy column detail path; identical to [getColumn]. Prefer
     * `GET /api/v1/schema/{schemaName}/tables/{tableName}/columns/{columnName}`.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @param scope optional comma-separated scopes (`global,user:alice`)
     * @param context deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @param facetMode facet expansion policy: none | direct | hierarchy
     * @return column detail
     */
    @Deprecated(
        "Use GET /api/v1/schema/{schemaName}/tables/{tableName}/columns/{columnName} instead. " +
            "This path will be removed."
    )
    @GetMapping("/schemas/{schemaName}/tables/{tableName}/columns/{columnName}")
    @Operation(
        summary = "Get column detail (legacy path)",
        description = "Deprecated: use `GET /api/v1/schema/{schemaName}/tables/{tableName}/columns/{columnName}` " +
            "without the `/schemas` prefix.",
        deprecated = true
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Column detail payload",
                content = [Content(schema = Schema(implementation = ColumnDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed scope parameter"),
            ApiResponse(responseCode = "404", description = "Schema, table, or column not found")
        ]
    )
    fun getColumnLegacy(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @PathVariable columnName: String,
        @Parameter(description = "Optional comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): ColumnDto = schemaExplorerService.getColumn(schemaName, tableName, columnName, scope, context, origin, facetMode)
}
