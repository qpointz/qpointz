package io.qpointz.mill.metadata.api.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * One persisted facet assignment row as exposed on the public metadata API (SPEC §10.2).
 *
 * Merge semantics ([io.qpointz.mill.metadata.domain.facet.MergeAction]) are **not** included here;
 * use [FacetMergeTraceResponseDto] / `GET …/facets/merge-trace` for diagnostics.
 *
 * JSON keys align with canonical YAML facet rows: [facetTypeUrn], [scopeUrn]. Legacy `facetType` and
 * `scope` are accepted on deserialize.
 *
 * @property uid stable assignment UUID (DB `uuid` / `{facetUid}` in paths)
 * @property facetTypeUrn full facet-type URN
 * @property scopeUrn full scope URN for this row
 * @property origin whether the row is persisted ([FacetOrigin.CAPTURED]) or read-time derived ([FacetOrigin.INFERRED])
 * @property originId contributing source id (e.g. repository vs logical-layout source)
 * @property assignmentUid persisted assignment uid when [origin] is captured; null for inferred-only rows
 * @property payload facet JSON object
 * @property createdAt row creation time
 * @property lastModifiedAt last mutation time
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    name = "FacetInstance",
    description = "Facet row: type, scope, provenance (origin / originId / assignmentUid), payload, and stable uid. No mergeAction — use merge-trace for overlay diagnostics."
)
data class FacetInstanceDto(
    @field:Schema(
        description = "Stable facet assignment UUID",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    val uid: String,
    @field:JsonProperty("facetTypeUrn")
    @field:JsonAlias("facetType")
    @field:Schema(
        description = "Facet type URN",
        name = "facetTypeUrn",
        example = "urn:mill/metadata/facet-type:descriptive",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val facetTypeUrn: String,
    @field:JsonProperty("scopeUrn")
    @field:JsonAlias("scope")
    @field:Schema(
        description = "Scope URN",
        name = "scopeUrn",
        example = "urn:mill/metadata/scope:global",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val scopeUrn: String,
    @field:Schema(
        description = "Facet provenance: CAPTURED (persisted assignment) or INFERRED (read-time)",
        allowableValues = ["CAPTURED", "INFERRED"],
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val origin: FacetOrigin,
    @field:Schema(
        description = "Contributing metadata source id",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "repository-local"
    )
    val originId: String,
    @field:Schema(description = "Stable assignment uid when origin is CAPTURED; absent for pure INFERRED rows")
    val assignmentUid: String?,
    @field:Schema(description = "Facet payload", requiredMode = Schema.RequiredMode.REQUIRED)
    val payload: Map<String, Any?>,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val createdAt: Instant,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val lastModifiedAt: Instant
)
