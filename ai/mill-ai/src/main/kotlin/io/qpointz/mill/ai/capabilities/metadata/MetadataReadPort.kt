package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.ai.core.capability.CapabilityDependency
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

/**
 * Read-only metadata port for AI `metadata` and `metadata-authoring` capabilities.
 */
interface MetadataReadPort {
  /** Lists all facet type manifests (DEFINED + OBSERVED-only keys without definitions). */
  fun listFacetTypes(): List<FacetTypeManifest>

  /**
   * @param facetTypeKey facet type key or full URN
   * @return manifest or `null` when unknown
   */
  fun getFacetType(facetTypeKey: String): FacetTypeManifest?

  /** Returns merged facet instances for one metadata entity. */
  fun listEntityFacets(
    metadataEntityId: String,
    scope: String? = null,
    context: String? = null,
    origin: String? = null,
  ): List<Map<String, Any?>>

  /**
   * @param targetUrn optional filter by target URN (facet type or category)
   * @param contentKind optional filter (e.g. `facet-type-example`)
   */
  fun listContent(targetUrn: String? = null, contentKind: String? = null): List<MetadataContentWire>

  /**
   * @param contentUrn stable content row URN
   * @return content row or `null`
   */
  fun getContent(contentUrn: String): MetadataContentWire?

  /** Distinct facet categories with joined `facet-type-category` guidance. */
  fun listFacetCategories(): List<FacetCategoryWire>

  /**
   * @param facetTypeKey facet type key or URN
   * @param payload JSON payload to validate
   * @param metadataEntityId optional target entity for `applicableTo` enforcement
   * @return validation errors; empty when valid
   */
  fun validateFacetPayload(
    facetTypeKey: String,
    payload: Map<String, Any?>,
    metadataEntityId: String? = null,
  ): List<String>
}

data class MetadataCapabilityDependency(val port: MetadataReadPort) : CapabilityDependency
