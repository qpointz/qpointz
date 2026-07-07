package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.capabilities.metadata.FacetCategoryWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataContentWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataFacetValidation
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.data.schema.SchemaEntityTypeUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadField
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

/**
 * Expanded metadata catalog for harness facet capture packs ([GAPS.md] §12).
 */
class HarnessMetadataReadPort : MetadataReadPort {

  private val descriptiveFacet = facet(
    key = "descriptive",
    title = "Descriptive",
    category = "general",
    applicableTo = emptyList(),
    fields = listOf(
      field("summary", FacetSchemaType.STRING, required = true),
    ),
  )

  private val relationSourceFacet = facet(
    key = "relation-source",
    title = "Relation source",
    category = "relation",
    applicableTo = listOf(SchemaEntityTypeUrns.TABLE),
    fields = listOf(
      field("joinSql", FacetSchemaType.STRING, required = true),
    ),
  )

  private val relationTargetFacet = facet(
    key = "relation-target",
    title = "Relation target",
    category = "relation",
    applicableTo = listOf(SchemaEntityTypeUrns.TABLE),
    fields = listOf(
      field("joinSql", FacetSchemaType.STRING, required = true),
    ),
  )

  private val dqNullCheckFacet = facet(
    key = "dq-null-check",
    title = "Null check",
    category = "data-quality",
    applicableTo = listOf(SchemaEntityTypeUrns.ATTRIBUTE),
    fields = listOf(
      field("name", FacetSchemaType.STRING, required = true),
    ),
  )

  private val dqPredicateFacet = facet(
    key = "dq-predicate",
    title = "Predicate rule",
    category = "data-quality",
    applicableTo = listOf(SchemaEntityTypeUrns.TABLE),
    fields = listOf(
      field("name", FacetSchemaType.STRING, required = true),
      field("predicate", FacetSchemaType.STRING, required = true),
    ),
  )

  private val conceptFacet = facet(
    key = "concept",
    title = "Concept",
    category = "general",
    applicableTo = listOf(SchemaEntityTypeUrns.MODEL),
    fields = listOf(
      field(
        name = "concepts",
        type = FacetSchemaType.ARRAY,
        required = true,
        items = FacetPayloadSchema(
          type = FacetSchemaType.OBJECT,
          title = "concept",
          description = "concept",
        ),
      ),
    ),
  )

  private val allFacets = listOf(
    descriptiveFacet,
    relationSourceFacet,
    relationTargetFacet,
    dqNullCheckFacet,
    dqPredicateFacet,
    conceptFacet,
  )

  private val categories = listOf(
    FacetCategoryWire("general", "General metadata"),
    FacetCategoryWire("relation", "Relations"),
    FacetCategoryWire("data-quality", "Data quality"),
  )

  override fun listFacetTypes(): List<FacetTypeManifest> = allFacets

  override fun getFacetType(facetTypeKey: String): FacetTypeManifest? =
    allFacets.firstOrNull { it.typeKey == facetTypeKey || it.typeKey.endsWith(facetTypeKey) }

  override fun listEntityFacets(
    metadataEntityId: String,
    scope: String?,
    context: String?,
    origin: String?,
  ): List<Map<String, Any?>> = emptyList()

  override fun listContent(targetUrn: String?, contentKind: String?): List<MetadataContentWire> = emptyList()

  override fun getContent(contentUrn: String): MetadataContentWire? = null

  override fun listFacetCategories(): List<FacetCategoryWire> = categories

  override fun validateFacetPayload(
    facetTypeKey: String,
    payload: Map<String, Any?>,
    metadataEntityId: String?,
  ): List<String> = MetadataFacetValidation.validate(this, facetTypeKey, payload, metadataEntityId)

  private fun facet(
    key: String,
    title: String,
    category: String,
    applicableTo: List<String>,
    fields: List<FacetPayloadField>,
  ): FacetTypeManifest =
    FacetTypeManifest(
      typeKey = key,
      title = title,
      description = "$title harness facet",
      category = category,
      targetCardinality = FacetTargetCardinality.MULTIPLE,
      applicableTo = applicableTo,
      payload = FacetPayloadSchema(
        type = FacetSchemaType.OBJECT,
        title = title,
        description = "$title payload",
        fields = fields,
      ),
    )

  private fun field(
    name: String,
    type: FacetSchemaType,
    required: Boolean = false,
    items: FacetPayloadSchema? = null,
  ): FacetPayloadField =
    FacetPayloadField(
      name = name,
      required = required,
      schema = FacetPayloadSchema(
        type = type,
        title = name,
        description = name,
        items = items,
      ),
    )
}
