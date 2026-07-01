package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.data.metadata.CatalogPath
import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.metadata.domain.MetadataEntityUrn

/**
 * Resolves qualified catalog paths and typed URNs to canonical metadata entity instance URNs for AI tools.
 */
object MetadataEntityIds {

  /** Catalog alias for the logical model root entity. */
  const val MODEL_ROOT_CATALOG_PATH = "model-entity"

  /**
   * Resolved metadata target entity for facet tools.
   *
   * @property catalogPath qualified database object name (`schema`, `schema.table`, or `schema.table.column`)
   * @property metadataEntityUrn canonical `urn:mill/model/…` entity URN
   * @property entityKind model URN kind (`schema`, `table`, `attribute`, …)
   */
  data class ResolvedEntity(
    val catalogPath: String,
    val metadataEntityUrn: String,
    val entityKind: String,
  )

  /**
   * @param raw qualified catalog path or full `urn:mill/model/…` URN
   * @return canonical entity reference rebuilt from decoded catalog coordinates
   */
  fun resolve(raw: String): String = resolveEntity(raw).metadataEntityUrn

  /**
   * @param raw qualified catalog path or full `urn:mill/model/…` URN
   * @return resolved catalog path and canonical entity URN
   */
  fun resolveEntity(raw: String): ResolvedEntity {
    val trimmed = raw.trim()
    require(trimmed.isNotEmpty()) { "Invalid metadata entity id: $raw" }
    if (MetadataEntityUrn.isMillUrn(trimmed)) {
      val canonical = MetadataEntityUrn.canonicalize(trimmed)
      require(ModelEntityUrn.isModelEntityUrn(canonical)) {
        "URN is not a model entity URN: $canonical"
      }
      if (ModelEntityUrn.isModelRootUrn(canonical)) {
        return ResolvedEntity(MODEL_ROOT_CATALOG_PATH, ModelEntityUrn.MODEL_ENTITY_ID, ModelEntityUrn.KIND_MODEL)
      }
      if (ModelEntityUrn.isConceptUrn(canonical)) {
        throw IllegalArgumentException(
          "Concept refs must use ConceptRefs, not MetadataEntityIds: $canonical",
        )
      }
      val path = ModelEntityUrn.parseCatalogPath(canonical)
      val catalogPath = path.qualifiedName()
        ?: throw IllegalArgumentException("Cannot decode catalog path from URN: $canonical")
      val metadataEntityUrn = urnForCatalogPath(catalogPath)
      val entityKind = ModelEntityUrn.kindOf(metadataEntityUrn)
        ?: throw IllegalArgumentException("Cannot resolve entity kind for catalogPath=$catalogPath")
      return ResolvedEntity(catalogPath, metadataEntityUrn, entityKind)
    }
    if (trimmed.equals(MODEL_ROOT_CATALOG_PATH, ignoreCase = true)) {
      return ResolvedEntity(MODEL_ROOT_CATALOG_PATH, ModelEntityUrn.MODEL_ENTITY_ID, ModelEntityUrn.KIND_MODEL)
    }
    val catalogPath = toCatalogPath(trimmed)
    val metadataEntityUrn = urnForCatalogPath(catalogPath)
    val entityKind = ModelEntityUrn.kindOf(metadataEntityUrn)
      ?: throw IllegalArgumentException("Cannot resolve entity kind for catalogPath=$catalogPath")
    return ResolvedEntity(catalogPath, metadataEntityUrn, entityKind)
  }

  /**
   * @param raw qualified catalog path or model entity URN
   * @return dot-separated qualified catalog path in lowercase
   */
  fun toCatalogPath(raw: String): String {
    val trimmed = raw.trim()
    require(trimmed.isNotEmpty()) { "Invalid metadata entity id: $raw" }
    if (trimmed.equals(MODEL_ROOT_CATALOG_PATH, ignoreCase = true)) {
      return MODEL_ROOT_CATALOG_PATH
    }
    if (MetadataEntityUrn.isMillUrn(trimmed)) {
      val canonical = MetadataEntityUrn.canonicalize(trimmed)
      require(ModelEntityUrn.isModelEntityUrn(canonical)) {
        "URN is not a model entity URN: $canonical"
      }
      if (ModelEntityUrn.isModelRootUrn(canonical)) {
        return MODEL_ROOT_CATALOG_PATH
      }
      if (ModelEntityUrn.isConceptUrn(canonical)) {
        throw IllegalArgumentException("Concept refs must use ConceptRefs, not MetadataEntityIds: $canonical")
      }
      val path = ModelEntityUrn.parseCatalogPath(canonical)
      return path.qualifiedName()
        ?: throw IllegalArgumentException("Cannot decode catalog path from URN: $canonical")
    }
    val parts = trimmed.split('.').map { it.trim() }.filter { it.isNotEmpty() }
    require(parts.isNotEmpty()) { "Invalid metadata entity id: $raw" }
    require(parts.all { it.isNotEmpty() }) { "Invalid metadata entity id: $raw" }
    return parts.joinToString(".") { it.lowercase() }
  }

  private fun urnForCatalogPath(catalogPath: String): String {
    if (catalogPath == MODEL_ROOT_CATALOG_PATH) {
      return ModelEntityUrn.MODEL_ENTITY_ID
    }
    val parts = catalogPath.split('.')
    return when (parts.size) {
      1 -> ModelEntityUrn.forSchema(parts[0])
      2 -> ModelEntityUrn.forTable(parts[0], parts[1])
      else -> ModelEntityUrn.forAttribute(parts[0], parts[1], parts.drop(2).joinToString("."))
    }
  }

  private fun CatalogPath.qualifiedName(): String? {
    val schemaName = schema?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val tableName = table?.trim()?.takeIf { it.isNotEmpty() }
    val columnName = column?.trim()?.takeIf { it.isNotEmpty() }
    return when {
      tableName == null -> schemaName.lowercase()
      columnName == null -> "${schemaName.lowercase()}.${tableName.lowercase()}"
      else -> "${schemaName.lowercase()}.${tableName.lowercase()}.$columnName"
    }
  }
}
