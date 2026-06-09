package io.qpointz.mill.analysis.queries.web

import io.qpointz.mill.analysis.queries.web.dto.AnalysisDialectWireDto
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Exposes the configured SQL dialect for mill-ui Analysis ({@code GET /api/v1/analysis/dialect}).
 *
 * @param dialectSpec dialect resolved from {@code mill.data.sql.dialect}
 */
@RestController
@RequestMapping("/api/v1/analysis/dialect")
@Tag(name = "analysis", description = "Analysis view configuration and catalog")
class AnalysisDialectRestController(
    private val dialectSpec: SqlDialectSpec,
) {

    /**
     * @return active SQL dialect metadata for the Analysis SQL editor
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get configured SQL dialect")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dialect returned"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun getDialect(): AnalysisDialectWireDto = AnalysisDialectWireMapper.toWire(dialectSpec)
}
