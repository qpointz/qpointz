package io.qpointz.mill.metadata.api.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * REST DTO representing a metadata scope.
 *
 * Used for `GET /api/v1/metadata/scopes` and `POST /api/v1/metadata/scopes` endpoints.
 * The [scopeUrn] is always the full Mill scope URN (e.g. `urn:mill/metadata/scope:global`).
 * Legacy JSON key `scopeId` is accepted on deserialize.
 *
 * @property scopeUrn     full Mill scope URN key
 * @property displayName optional human-readable label for the scope
 * @property ownerId     optional owner identifier; null for shared or system scopes
 * @property createdAt   timestamp when this scope was created
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MetadataScopeDto @JsonCreator constructor(
    @JsonProperty("scopeUrn")
    @JsonAlias("scopeId")
    val scopeUrn: String,
    @JsonProperty("displayName")
    val displayName: String? = null,
    @JsonProperty("ownerId")
    val ownerId: String? = null,
    @JsonProperty("createdAt")
    val createdAt: Instant? = null
)
