package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

/** Empty catalog; validation rejects all payloads with a deterministic message. */
class EmptyMetadataReadPort : MetadataReadPort {

    override fun listFacetTypes(): List<FacetTypeManifest> = emptyList()

    override fun listEntityFacets(
        metadataEntityId: String,
        scope: String?,
        context: String?,
        origin: String?,
    ): List<Map<String, Any?>> = emptyList()

    override fun validateFacetPayload(facetTypeKey: String, payload: Map<String, Any?>): List<String> =
        listOf("no facet type catalog available (empty MetadataReadPort); cannot validate facetTypeKey=$facetTypeKey")
}
