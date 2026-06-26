package io.qpointz.mill.ai.data.metadata

import io.qpointz.mill.ai.capabilities.metadata.FacetCategoryWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataContentWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataFacetValidation
import io.qpointz.mill.ai.capabilities.metadata.MetadataEntityIds
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.metadata.domain.FacetTypeSource
import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetTypeDefinitionManifestMapper
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.repository.MetadataContentRepository
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.FacetService
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.utils.JsonUtils
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality

/**
 * Production [MetadataReadPort] backed by in-process metadata services (no HTTP loopback).
 *
 * @param facetCatalog facet type definitions and runtime types
 * @param facetService merged facet instance reads
 * @param contentRepository authoring content rows ([MetadataContent])
 */
class ServiceMetadataReadPort(
  private val facetCatalog: FacetCatalog,
  private val facetService: FacetService,
  private val contentRepository: MetadataContentRepository,
) : MetadataReadPort {

  private val jsonMapper = JsonUtils.defaultJsonMapper()

  override fun listFacetTypes(): List<FacetTypeManifest> {
    val defined = facetCatalog.listDefinitions()
      .filter { it.enabled }
      .map { FacetTypeDefinitionManifestMapper.toManifest(it) }
    val definedKeys = defined.map { MetadataEntityUrn.canonicalize(it.typeKey) }.toSet()
    val observedOnly = facetCatalog.listTypes()
      .asSequence()
      .filter { it.source == FacetTypeSource.OBSERVED && it.definition == null }
      .map { it.typeKey }
      .filter { key -> key !in definedKeys }
      .map { key ->
        FacetTypeManifest(
          typeKey = key,
          title = key.substringAfterLast(':'),
          description = "Observed facet type (no definition registered)",
          targetCardinality = FacetTargetCardinality.MULTIPLE,
          payload = FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = key.substringAfterLast(':'),
            description = "Observed facet type",
          ),
        )
      }
      .toList()
    return defined + observedOnly
  }

  override fun getFacetType(facetTypeKey: String): FacetTypeManifest? {
    val key = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(facetTypeKey))
    val def = facetCatalog.findDefinition(key) ?: return null
    return FacetTypeDefinitionManifestMapper.toManifest(def)
  }

  override fun listEntityFacets(
    metadataEntityId: String,
    scope: String?,
    context: String?,
    origin: String?,
  ): List<Map<String, Any?>> {
    val entityId = MetadataEntityIds.resolve(metadataEntityId)
    val readContext = MetadataReadContext.parse(scope ?: context, origin)
    return facetService.resolve(entityId, readContext).map { it.toWireMap() }
  }

  override fun listContent(targetUrn: String?, contentKind: String?): List<MetadataContentWire> {
    val rows = when {
      targetUrn != null -> contentRepository.findByTarget(
        MetadataEntityUrn.canonicalize(targetUrn),
        contentKind,
      )
      contentKind != null -> contentRepository.findAll().filter { it.contentKind == contentKind }
      else -> contentRepository.findAll()
    }
    return rows
      .filter { it.enabled }
      .sortedWith(compareBy({ it.targetUrn }, { it.sortOrder }, { it.contentUrn }))
      .map { it.toWire() }
  }

  override fun getContent(contentUrn: String): MetadataContentWire? =
    contentRepository.findByContentUrn(MetadataEntityUrn.canonicalize(contentUrn))
      ?.takeIf { it.enabled }
      ?.toWire()

  override fun listFacetCategories(): List<FacetCategoryWire> {
    val categoriesFromDefs = facetCatalog.listDefinitions()
      .asSequence()
      .mapNotNull { it.category?.trim()?.takeIf { c -> c.isNotEmpty() } }
      .toSet()
    val guidanceRows = contentRepository.findAll()
      .asSequence()
      .filter { it.enabled && it.contentKind == MetadataContent.KIND_FACET_TYPE_CATEGORY }
      .toList()
    val categoriesFromGuidance = guidanceRows.mapNotNull { row ->
      row.targetUrn.removePrefix(MetadataUrns.FACET_TYPE_CATEGORY_PREFIX).takeIf { it.isNotEmpty() }
    }
    val allCategories = (categoriesFromDefs + categoriesFromGuidance).sorted()
    val guidanceByCategory = guidanceRows.associateBy { row ->
      row.targetUrn.removePrefix(MetadataUrns.FACET_TYPE_CATEGORY_PREFIX)
    }
    return allCategories.map { category ->
      val guidance = guidanceByCategory[category]?.toWire()
      FacetCategoryWire(
        category = category,
        title = guidance?.title,
        description = guidance?.description,
        guidance = guidance?.content as? Map<String, Any?>,
      )
    }
  }

  override fun validateFacetPayload(
    facetTypeKey: String,
    payload: Map<String, Any?>,
    metadataEntityId: String?,
  ): List<String> = MetadataFacetValidation.validate(this, facetTypeKey, payload, metadataEntityId)

  private fun FacetInstance.toWireMap(): Map<String, Any?> =
    mapOf(
      "uid" to uid,
      "facetTypeUrn" to facetTypeKey,
      "scopeUrn" to scopeKey,
      "origin" to origin.name,
      "originId" to originId,
      "assignmentUid" to assignmentUid,
      "payload" to payload,
      "createdAt" to createdAt.toString(),
      "lastModifiedAt" to lastModifiedAt.toString(),
    )

  private fun MetadataContent.toWire(): MetadataContentWire =
    MetadataContentWire(
      contentUrn = contentUrn,
      contentKind = contentKind,
      targetUrn = targetUrn,
      scopeUrn = scopeUrn,
      title = title,
      description = description,
      content = parseContentBody(contentBody, mediaType),
      mediaType = mediaType,
      sortOrder = sortOrder,
    )

  private fun parseContentBody(body: String, mediaType: String): Any? =
    if (mediaType == MetadataContent.MEDIA_TYPE_JSON) {
      try {
        @Suppress("UNCHECKED_CAST")
        jsonMapper.readValue(body, Map::class.java) as Map<String, Any?>
      } catch (_: Exception) {
        body
      }
    } else {
      body
    }
}

/** Adapts [FacetCatalog] stack to [MetadataReadPort] for non-Spring call sites. */
fun metadataReadPort(
  facetCatalog: FacetCatalog,
  facetService: FacetService,
  contentRepository: MetadataContentRepository,
): MetadataReadPort = ServiceMetadataReadPort(facetCatalog, facetService, contentRepository)
