package io.qpointz.mill.analysis.queries.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request body for creating or updating a saved query.
 *
 * @param name display title shown in the Analysis sidebar
 * @param description optional summary shown in the editor header
 * @param sql SQL text loaded into the editor
 * @param tags optional labels for search and grouping
 */
@Schema(description = "Saved query create/update payload")
data class SavedQueryWriteRequest(
    @field:NotBlank
    @field:Size(max = 512)
    val name: String,

    @field:Size(max = 2048)
    val description: String? = null,

    val sql: String = "",

    val tags: List<@Size(max = 64) String>? = null,
)

/**
 * Optional client-supplied id when creating a saved query.
 *
 * @param id stable catalog identifier; generated from {@link #name} when omitted
 * @param name display title
 * @param description optional summary
 * @param sql SQL text
 * @param tags optional labels
 */
@Schema(description = "Saved query create payload with optional id")
data class SavedQueryCreateRequest(
    @field:Size(max = 128)
    val id: String? = null,

    @field:NotBlank
    @field:Size(max = 512)
    val name: String,

    @field:Size(max = 2048)
    val description: String? = null,

    val sql: String = "",

    val tags: List<@Size(max = 64) String>? = null,
)
