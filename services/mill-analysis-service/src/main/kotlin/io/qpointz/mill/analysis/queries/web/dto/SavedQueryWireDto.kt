package io.qpointz.mill.analysis.queries.web.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Wire representation of a saved query for mill-ui Analysis.
 *
 * @param id stable catalog identifier
 * @param name display title
 * @param description optional summary
 * @param sql SQL text
 * @param createdAt creation time as Unix epoch milliseconds
 * @param updatedAt last update time as Unix epoch milliseconds
 * @param tags optional labels
 */
@Schema(description = "Saved SQL query for the Analysis catalog")
data class SavedQueryWireDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val sql: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: List<String>? = null,
)

/**
 * List envelope for {@code GET /api/v1/analysis/queries}.
 *
 * @param queries saved queries ordered by most recently updated first
 */
data class SavedQueryListResponse(
    val queries: List<SavedQueryWireDto>,
)
