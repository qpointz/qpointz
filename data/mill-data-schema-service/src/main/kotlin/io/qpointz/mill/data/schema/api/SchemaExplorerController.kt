package io.qpointz.mill.data.schema.api

import io.qpointz.mill.data.schema.api.dto.ColumnDto
import io.qpointz.mill.data.schema.api.dto.SchemaContextDto
import io.qpointz.mill.data.schema.api.dto.SchemaDto
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
 * REST controller exposing schema explorer endpoints.
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
     * @param context optional comma-separated scopes (`global,user:alice`)
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
            ApiResponse(responseCode = "400", description = "Malformed context parameter")
        ]
    )
    fun listSchemas(
        @Parameter(description = "Optional comma-separated context scopes")
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): List<SchemaListItemDto> = schemaExplorerService.listSchemas(context, facetMode)

    /**
     * Backward-compatible alias for legacy clients.
     */
    @GetMapping("/schemas")
    fun listSchemasLegacy(
        @RequestParam(required = false) context: String?,
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): List<SchemaListItemDto> = schemaExplorerService.listSchemas(context, facetMode)

    /**
     * Returns schema tree payload for initial explorer loading.
     *
     * @param context optional comma-separated scopes (`global,user:alice`)
     * @return schema detail list containing table summaries
     */
    @GetMapping("/tree")
    @Operation(summary = "Get schema tree payload")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Schema tree payload",
                content = [Content(array = ArraySchema(schema = Schema(implementation = SchemaDto::class)))]
            ),
            ApiResponse(responseCode = "400", description = "Malformed context parameter")
        ]
    )
    fun getTree(
        @Parameter(description = "Optional comma-separated context scopes")
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "none") facetMode: String
    ): List<SchemaDto> = schemaExplorerService.getTree(context, facetMode)

    /**
     * Returns one schema detail payload.
     *
     * @param schemaName schema name
     * @param context optional comma-separated scopes (`global,user:alice`)
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
            ApiResponse(responseCode = "400", description = "Malformed context parameter"),
            ApiResponse(responseCode = "404", description = "Schema not found")
        ]
    )
    fun getSchema(
        @PathVariable schemaName: String,
        @Parameter(description = "Optional comma-separated context scopes")
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): SchemaDto = schemaExplorerService.getSchema(schemaName, context, facetMode)

    /**
     * Backward-compatible alias for legacy clients.
     */
    @GetMapping("/schemas/{schemaName}")
    fun getSchemaLegacy(
        @PathVariable schemaName: String,
        @RequestParam(required = false) context: String?,
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): SchemaDto = schemaExplorerService.getSchema(schemaName, context, facetMode)

    /**
     * Returns one table detail payload.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param context optional comma-separated scopes (`global,user:alice`)
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
            ApiResponse(responseCode = "400", description = "Malformed context parameter"),
            ApiResponse(responseCode = "404", description = "Schema or table not found")
        ]
    )
    fun getTable(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @Parameter(description = "Optional comma-separated context scopes")
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): TableDto = schemaExplorerService.getTable(schemaName, tableName, context, facetMode)

    /**
     * Backward-compatible alias for legacy clients.
     */
    @GetMapping("/schemas/{schemaName}/tables/{tableName}")
    fun getTableLegacy(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @RequestParam(required = false) context: String?,
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): TableDto = schemaExplorerService.getTable(schemaName, tableName, context, facetMode)

    /**
     * Returns one column detail payload.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @param context optional comma-separated scopes (`global,user:alice`)
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
            ApiResponse(responseCode = "400", description = "Malformed context parameter"),
            ApiResponse(responseCode = "404", description = "Schema, table, or column not found")
        ]
    )
    fun getColumn(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @PathVariable columnName: String,
        @Parameter(description = "Optional comma-separated context scopes")
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Facet expansion policy: none | direct | hierarchy")
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): ColumnDto = schemaExplorerService.getColumn(schemaName, tableName, columnName, context, facetMode)

    /**
     * Backward-compatible alias for legacy clients.
     */
    @GetMapping("/schemas/{schemaName}/tables/{tableName}/columns/{columnName}")
    fun getColumnLegacy(
        @PathVariable schemaName: String,
        @PathVariable tableName: String,
        @PathVariable columnName: String,
        @RequestParam(required = false) context: String?,
        @RequestParam(required = false, defaultValue = "direct") facetMode: String
    ): ColumnDto = schemaExplorerService.getColumn(schemaName, tableName, columnName, context, facetMode)
}
