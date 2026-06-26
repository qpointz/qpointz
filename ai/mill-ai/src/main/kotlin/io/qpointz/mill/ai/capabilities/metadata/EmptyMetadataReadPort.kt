package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

/**
 * No-op metadata port used when the metadata module is absent from the host classpath.
 */
class EmptyMetadataReadPort : MetadataReadPort {
  override fun listFacetTypes(): List<FacetTypeManifest> = emptyList()

  override fun getFacetType(facetTypeKey: String): FacetTypeManifest? = null

  override fun listEntityFacets(
    metadataEntityId: String,
    scope: String?,
    context: String?,
    origin: String?,
  ): List<Map<String, Any?>> = emptyList()

  override fun listContent(targetUrn: String?, contentKind: String?): List<MetadataContentWire> = emptyList()

  override fun getContent(contentUrn: String): MetadataContentWire? = null

  override fun listFacetCategories(): List<FacetCategoryWire> = emptyList()

  override fun validateFacetPayload(
    facetTypeKey: String,
    payload: Map<String, Any?>,
    metadataEntityId: String?,
  ): List<String> =
    listOf("no facet type catalog available (empty MetadataReadPort); cannot validate facetTypeKey=$facetTypeKey")
}
