package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.ai.capabilities.metadata.MetadataEntityIds
import io.qpointz.mill.data.metadata.ModelEntityUrn

/**
 * Grounds qualified catalog paths against [SchemaCatalogPort] and returns canonical entity URNs.
 */
object SchemaMetadataEntityResolver {

  /**
   * @param catalog catalog port used for existence checks
   * @param catalogPath qualified path (`schema`, `schema.table`, or `schema.table.column`)
   * @return resolved entity map for tool output, or error map when not found
   */
  fun resolve(catalog: SchemaCatalogPort, catalogPath: String): Map<String, Any?> {
    return try {
      val resolved = MetadataEntityIds.resolveEntity(catalogPath)
      val exists = when (resolved.entityKind) {
        "schema" -> catalog.listSchemas().any { it.schemaName.equals(resolved.catalogPath, ignoreCase = true) }
        "table" -> {
          val parts = resolved.catalogPath.split('.', limit = 2)
          catalog.listTables(parts[0]).any { it.catalogPath.equals(resolved.catalogPath, ignoreCase = true) }
        }
        "attribute" -> {
          val path = ModelEntityUrn.parseCatalogPath(resolved.metadataEntityUrn)
          val schemaName = path.schema ?: return mapOf("error" to "invalid attribute path: ${resolved.catalogPath}")
          val tableName = path.table ?: return mapOf("error" to "invalid attribute path: ${resolved.catalogPath}")
          catalog.listColumns(schemaName, tableName).any { it.catalogPath.equals(resolved.catalogPath, ignoreCase = true) }
        }
        else -> false
      }
      if (!exists) {
        return mapOf("error" to "catalog object not found: ${resolved.catalogPath}")
      }
      mapOf(
        "catalogPath" to resolved.catalogPath,
        "metadataEntityUrn" to resolved.metadataEntityUrn,
        "entityKind" to resolved.entityKind,
      )
    } catch (ex: IllegalArgumentException) {
      mapOf("error" to (ex.message ?: "invalid catalog path: $catalogPath"))
    }
  }
}
