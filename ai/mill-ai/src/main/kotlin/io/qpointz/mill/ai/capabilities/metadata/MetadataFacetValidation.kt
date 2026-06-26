package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.data.schema.SchemaEntityTypeUrns
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

/**
 * Shared validation for facet payload structure and optional `applicableTo` checks.
 */
object MetadataFacetValidation {

  /**
   * @param port metadata read port for catalog lookup
   * @param facetTypeKey facet type key or URN
   * @param payload JSON payload to validate
   * @param metadataEntityId optional target entity URN for `applicableTo` enforcement
   * @return error messages; empty when valid
   */
  fun validate(
    port: MetadataReadPort,
    facetTypeKey: String,
    payload: Map<String, Any?>,
    metadataEntityId: String? = null,
  ): List<String> {
    val manifest = port.getFacetType(facetTypeKey)
      ?: port.listFacetTypes().let { FacetPayloadStructureValidator.manifestForFacetType(it, facetTypeKey) }
      ?: return listOf("unknown facet type: $facetTypeKey")

    val schemaErrors = FacetPayloadStructureValidator.validate(manifest.payload, payload)
    if (schemaErrors.isNotEmpty()) {
      return schemaErrors
    }
    if (metadataEntityId.isNullOrBlank()) {
      return emptyList()
    }
    return validateApplicableTo(manifest, metadataEntityId)
  }

  /**
   * @param manifest facet type manifest
   * @param metadataEntityId grounded target entity URN
   * @return errors when [FacetTypeManifest.applicableTo] excludes the entity kind
   */
  fun validateApplicableTo(manifest: FacetTypeManifest, metadataEntityId: String): List<String> {
    val applicable = manifest.applicableTo
    if (applicable.isNullOrEmpty()) {
      return emptyList()
    }
    val entityTypeUrn = entityKindToApplicableUrn(ModelEntityUrn.kindOf(metadataEntityId))
      ?: return listOf("cannot resolve entity kind from metadataEntityId=$metadataEntityId")
    val allowed = applicable.any { it.equals(entityTypeUrn, ignoreCase = true) }
    return if (allowed) {
      emptyList()
    } else {
      listOf(
        "facet type ${manifest.typeKey} is not applicable to entity kind $entityTypeUrn " +
          "(metadataEntityId=$metadataEntityId)",
      )
    }
  }

  /**
   * @param metadataEntityId grounded model entity URN
   * @return `applicableTo` entity-type URN or `null` when kind is unknown
   */
  fun applicableEntityTypeUrn(metadataEntityId: String): String? =
    entityKindToApplicableUrn(ModelEntityUrn.kindOf(metadataEntityId))

  private fun entityKindToApplicableUrn(kind: String?): String? =
    when (kind) {
      ModelEntityUrn.KIND_SCHEMA -> SchemaEntityTypeUrns.SCHEMA
      ModelEntityUrn.KIND_TABLE -> SchemaEntityTypeUrns.TABLE
      ModelEntityUrn.KIND_ATTRIBUTE -> SchemaEntityTypeUrns.ATTRIBUTE
      ModelEntityUrn.KIND_CONCEPT -> SchemaEntityTypeUrns.CONCEPT
      ModelEntityUrn.KIND_MODEL -> SchemaEntityTypeUrns.MODEL
      else -> null
    }
}
