package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.utils.JsonUtils

/**
 * Maps persisted [FacetTypeDefinition] rows to [FacetTypeManifest] for REST and AI wire.
 */
object FacetTypeDefinitionManifestMapper {

  private val mapper = JsonUtils.defaultJsonMapper()

  /**
   * @param def catalog definition row
   * @return manifest including [FacetTypeManifest.payload] (`contentSchema` on wire)
   */
  fun toManifest(def: FacetTypeDefinition): FacetTypeManifest {
    val payload: FacetPayloadSchema =
      if (!def.contentSchema.isNullOrEmpty()) {
        try {
          mapper.convertValue(def.contentSchema, FacetPayloadSchema::class.java)
        } catch (_: Exception) {
          emptyPayload(def)
        }
      } else {
        emptyPayload(def)
      }
    return FacetTypeManifest(
      typeKey = def.typeKey,
      title = def.displayName ?: def.typeKey,
      description = def.description ?: "",
      category = def.category,
      enabled = def.enabled,
      mandatory = def.mandatory,
      targetCardinality = def.targetCardinality,
      applicableTo = def.applicableTo,
      schemaVersion = def.schemaVersion,
      payload = payload,
    )
  }

  private fun emptyPayload(def: FacetTypeDefinition): FacetPayloadSchema =
    FacetPayloadSchema(
      type = FacetSchemaType.OBJECT,
      title = def.displayName ?: def.typeKey,
      description = def.description ?: "",
    )
}
