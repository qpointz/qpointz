package io.qpointz.mill.metadata.source

import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.service.MetadataReadContext

/**
 * Pluggable read-only contributor of facet rows for one origin.
 *
 * @property originId stable id used for muting in [MetadataReadContext] and UI attribution
 */
interface MetadataSource {

    val originId: String

    /**
     * Returns this origin's facets for [entityId] after applying [context] (scopes + origin muting).
     */
    fun fetchForEntity(entityId: String, context: MetadataReadContext): List<FacetInstance>
}
