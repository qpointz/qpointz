package io.qpointz.mill.metadata.api.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Response for `GET /api/v1/metadata/entities/{id}/facets/merge-trace` (SPEC §10.5).
 *
 * Lists every persisted assignment for the entity with [mergeAction] and whether the row
 * appears in the effective merged view for the requested [context] order.
 *
 * @property context scope URNs in evaluation order (same as facet list `?context=`)
 * @property entries one row per stored facet assignment
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Facet merge trace: raw assignments plus merge_action and effective-view flags.")
data class FacetMergeTraceResponseDto(
    @field:Schema(description = "Ordered scope URNs used for merge", requiredMode = Schema.RequiredMode.REQUIRED)
    val context: List<String>,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val entries: List<FacetMergeTraceEntryDto>
)

/**
 * @property uid assignment UUID
 * @property facetTypeUrn facet type URN
 * @property scopeUrn scope URN
 * @property mergeAction persisted merge contribution (`SET`, `TOMBSTONE`, `CLEAR`)
 * @property payload assignment payload
 * @property contributesToEffectiveView true if this row appears in the merged effective facet list for [FacetMergeTraceResponseDto.context]
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Single facet row in a merge-trace response.")
data class FacetMergeTraceEntryDto(
    val uid: String,
    @param:JsonProperty("facetTypeUrn")
    @param:JsonAlias("facetType")
    val facetTypeUrn: String,
    @param:JsonProperty("scopeUrn")
    @param:JsonAlias("scope")
    val scopeUrn: String,
    val mergeAction: String,
    val payload: Map<String, Any?>,
    val contributesToEffectiveView: Boolean
)
