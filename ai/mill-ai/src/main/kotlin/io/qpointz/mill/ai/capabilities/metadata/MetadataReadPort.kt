package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.ai.core.capability.CapabilityDependency
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

interface MetadataReadPort {
    fun listFacetTypes(): List<FacetTypeManifest>
    fun listEntityFacets(
        metadataEntityId: String,
        scope: String? = null,
        context: String? = null,
        origin: String? = null,
    ): List<Map<String, Any?>>

    fun validateFacetPayload(facetTypeKey: String, payload: Map<String, Any?>): List<String>
}

data class MetadataCapabilityDependency(val port: MetadataReadPort) : CapabilityDependency
