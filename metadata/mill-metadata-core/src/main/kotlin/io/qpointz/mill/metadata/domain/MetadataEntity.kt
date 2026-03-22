package io.qpointz.mill.metadata.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.Instant
import java.util.Optional

/**
 * Document-style metadata entity with scoped facet payloads.
 *
 * Facets are stored in a two-level map: `facets[facetTypeUrn][scopeUrn] = payload`.
 * Scope keys and facet type keys must use Mill URN notation (see [MetadataUrns]).
 *
 * @property id            unique identifier, typically `schemaName.tableName` or similar.
 * @property type          entity type discriminator.
 * @property schemaName    logical schema name (non-null for schema, table, attribute entities).
 * @property tableName     table name (non-null for table and attribute entities).
 * @property attributeName column/attribute name (non-null for attribute entities).
 * @property facets        two-level map: `facetTypeUrn → (scopeUrn → payload)`.
 * @property createdAt     creation timestamp.
 * @property updatedAt     last-modified timestamp.
 * @property createdBy     actor who created the entity.
 * @property updatedBy     actor who last modified the entity.
 */
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

    /**
     * Returns a facet for a specific type and scope converted to [facetClass].
     *
     * @param facetType  the facet type URN key
     * @param scope      the scope URN key
     * @param facetClass the target class for conversion
     * @return an [Optional] containing the converted facet, or empty if not found
     */
    fun <T : Any> getFacet(facetType: String, scope: String, facetClass: Class<T>): Optional<T> {
        val scopedFacets = facets[facetType] ?: return Optional.empty<T>()
        val facetData = scopedFacets[scope] ?: return Optional.empty<T>()
        return converter().convert(facetData, facetClass)
    }

    /**
     * Returns raw facet payload without conversion.
     *
     * @param facetType  the facet type URN key
     * @param scope      the scope URN key
     * @return raw payload, or `null` if absent
     */
    fun getRawFacet(facetType: String, scope: String): Any? {
        return facets[facetType]?.get(scope)
    }

    /**
     * Lists all scope URN keys available for a given facet type.
     *
     * @param facetType the facet type URN key
     * @return set of scope URN keys, or empty if the facet type is absent
     */
    fun getFacetScopes(facetType: String): Set<String> {
        return facets[facetType]?.keys ?: emptySet()
    }

    /**
     * Stores or replaces a scoped facet payload.
     *
     * @param facetType the facet type URN key
     * @param scope     the scope URN key
     * @param facetData the payload to store
     */
    fun setFacet(facetType: String, scope: String, facetData: Any?) {
        facets.getOrPut(facetType) { mutableMapOf() }[scope] = facetData
    }

    /**
     * Resolves facet value using scope precedence: global first, then roles, then teams,
     * then user-specific (last scope wins).
     *
     * Scope keys use Mill URN notation via [MetadataUrns]:
     * - global: [MetadataUrns.SCOPE_GLOBAL]
     * - role:   [MetadataUrns.scopeRole]
     * - team:   [MetadataUrns.scopeTeam]
     * - user:   [MetadataUrns.scopeUser]
     *
     * @param facetType  the facet type URN key
     * @param userId     the requesting user's identifier
     * @param userTeams  the requesting user's team memberships
     * @param userRoles  the requesting user's assigned roles
     * @param facetClass the target class for conversion
     * @return an [Optional] containing the merged facet value, or empty if no scope matched
     */
    fun <T : Any> getMergedFacet(
        facetType: String,
        userId: String,
        userTeams: List<String>,
        userRoles: List<String>,
        facetClass: Class<T>
    ): Optional<T> {
        val scopedFacets = facets[facetType] ?: return Optional.empty<T>()
        val facetDataList = mutableListOf<Any?>()

        scopedFacets[MetadataUrns.SCOPE_GLOBAL]?.let { facetDataList.add(it) }
        for (role in userRoles) {
            scopedFacets[MetadataUrns.scopeRole(role)]?.let { facetDataList.add(it) }
        }
        for (team in userTeams) {
            scopedFacets[MetadataUrns.scopeTeam(team)]?.let { facetDataList.add(it) }
        }
        scopedFacets[MetadataUrns.scopeUser(userId)]?.let { facetDataList.add(it) }

        if (facetDataList.isEmpty()) return Optional.empty<T>()
        return converter().convert(facetDataList.last(), facetClass)
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
