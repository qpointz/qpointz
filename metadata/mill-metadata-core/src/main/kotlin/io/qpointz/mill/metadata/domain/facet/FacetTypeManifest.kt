package io.qpointz.mill.metadata.domain.facet

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

/**
 * Facet type manifest for REST bodies and JSON fixtures.
 *
 * **JSON field names:** [facetTypeUrn] (full Mill URN for this facet type), **`title`** (human label),
 * and **`contentSchema`** (payload shape), aligned with canonical facet-type documents.
 * Canonical YAML may add extra keys (e.g. `kind: FacetTypeDefinition`); this type is a **subset**
 * of that shape.
 *
 * **Deserialize aliases:** `typeRes`, `typeKey` → [typeKey]; `displayName` → [title]; `payload` → [payload].
 *
 * Kotlin internal names remain [typeKey], [title], and [payload]; Jackson maps [payload] to/from
 * JSON **`contentSchema`**.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FacetTypeManifest(
    @param:JsonProperty("facetTypeUrn")
    @param:JsonAlias("typeRes", "typeKey")
    val typeKey: String,
    @param:JsonProperty("title")
    @param:JsonAlias("displayName")
    val title: String,
    val description: String,
    val category: String? = null,
    val enabled: Boolean = true,
    val mandatory: Boolean = false,
    val targetCardinality: FacetTargetCardinality = FacetTargetCardinality.SINGLE,
    val applicableTo: List<String>? = null,
    val schemaVersion: String? = null,
    @param:JsonProperty("contentSchema")
    @param:JsonAlias("payload")
    val payload: FacetPayloadSchema
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 2L
    }
}

/** Declares whether a facet type can appear once or multiple times per target entity. */
enum class FacetTargetCardinality {
    SINGLE,
    MULTIPLE
}
