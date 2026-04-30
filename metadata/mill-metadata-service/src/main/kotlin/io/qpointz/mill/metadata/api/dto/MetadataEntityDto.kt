package io.qpointz.mill.metadata.api.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * REST DTO for metadata entity **identity only** (SPEC §10.3).
 *
 * No relational coordinates, no embedded facets — callers load facets via
 * `GET /api/v1/metadata/entities/{id}/facets` or type-scoped facet routes.
 *
 * JSON uses [entityUrn] (canonical YAML: `entityUrn` / legacy `entityRes`). Legacy `id` is accepted
 * on deserialize.
 *
 * @property entityUrn full `urn:mill/…` entity URN (required on create in the request body)
 * @property kind optional opaque persisted kind (e.g. table, attribute)
 * @property createdAt creation instant (read responses)
 * @property lastModifiedAt last mutation instant (read responses)
 * @property createdBy creating actor id, if known (read responses)
 * @property lastModifiedBy last mutating actor id, if known (read responses)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    name = "MetadataEntity",
    description = "Metadata entity identity. No schema/table/column coordinates and no facets map — use facet endpoints.",
    example = """{"entityUrn":"urn:mill/model/table:public.orders","kind":"table"}"""
)
data class MetadataEntityDto(
    @field:JsonProperty("entityUrn")
    @field:JsonAlias("id")
    @field:Schema(
        description = "Full entity URN",
        name = "entityUrn",
        example = "urn:mill/model/table:public.orders",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val entityUrn: String? = null,
    @field:Schema(description = "Opaque entity kind label", nullable = true, example = "table")
    val kind: String? = null,
    @field:Schema(description = "Creation timestamp", nullable = true)
    val createdAt: Instant? = null,
    @field:Schema(description = "Last modification timestamp", nullable = true)
    val lastModifiedAt: Instant? = null,
    @field:Schema(description = "Actor who created the entity", nullable = true)
    val createdBy: String? = null,
    @field:Schema(description = "Actor who last modified the entity", nullable = true)
    val lastModifiedBy: String? = null
)
