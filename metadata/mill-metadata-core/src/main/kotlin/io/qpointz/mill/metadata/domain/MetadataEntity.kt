package io.qpointz.mill.metadata.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.Instant
import java.util.Optional

/** Document-style metadata entity with scoped facet payloads. */
open class MetadataEntity(
    var id: String? = null,
    var type: MetadataType? = null,
    var schemaName: String? = null,
    var tableName: String? = null,
    var attributeName: String? = null,
    @JsonProperty("facets")
    var facets: MutableMap<String, MutableMap<String, Any?>> = mutableMapOf(),
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    var createdBy: String? = null,
    var updatedBy: String? = null
) : Serializable {

    /** Optional converter override (useful for tests or custom object mappers). */
    @JsonIgnore
    @Transient
    var facetConverter: FacetConverter? = null

    private fun converter(): FacetConverter = facetConverter ?: FacetConverter.defaultConverter()

    /** Returns a facet for a specific type and scope converted to [facetClass]. */
    fun <T : Any> getFacet(facetType: String, scope: String, facetClass: Class<T>): Optional<T> {
        val scopedFacets = facets[facetType] ?: return Optional.empty<T>()
        val facetData = scopedFacets[scope] ?: return Optional.empty<T>()
        return converter().convert(facetData, facetClass)
    }

    /** Returns raw facet payload without conversion. */
    fun getRawFacet(facetType: String, scope: String): Any? {
        return facets[facetType]?.get(scope)
    }

    /** Lists all scopes available for a given facet type. */
    fun getFacetScopes(facetType: String): Set<String> {
        return facets[facetType]?.keys ?: emptySet()
    }

    /** Stores or replaces scoped facet payload. */
    fun setFacet(facetType: String, scope: String, facetData: Any?) {
        facets.getOrPut(facetType) { mutableMapOf() }[scope] = facetData
    }

    /** Resolves facet value using scope precedence: global, role, team, user. */
    fun <T : Any> getMergedFacet(
        facetType: String,
        userId: String,
        userTeams: List<String>,
        userRoles: List<String>,
        facetClass: Class<T>
    ): Optional<T> {
        val scopedFacets = facets[facetType] ?: return Optional.empty<T>()
        val facetDataList = mutableListOf<Any?>()

        scopedFacets["global"]?.let { facetDataList.add(it) }
        for (role in userRoles) {
            scopedFacets["role:$role"]?.let { facetDataList.add(it) }
        }
        for (team in userTeams) {
            scopedFacets["team:$team"]?.let { facetDataList.add(it) }
        }
        scopedFacets["user:$userId"]?.let { facetDataList.add(it) }

        if (facetDataList.isEmpty()) return Optional.empty<T>()
        return converter().convert(facetDataList.last(), facetClass)
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
