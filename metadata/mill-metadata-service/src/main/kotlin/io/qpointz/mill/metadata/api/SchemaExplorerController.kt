package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.SearchResultDto
import io.qpointz.mill.metadata.api.dto.TreeNodeDto
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.service.MetadataService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Endpoints for schema navigation and metadata discovery. */
@RestController
@RequestMapping("/api/metadata/v1/explorer")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:8080"])
class SchemaExplorerController(private val metadataService: MetadataService) {

    @GetMapping("/tree")
    fun getTree(
        @RequestParam(name = "schema", required = false) schema: String?,
        @RequestParam(name = "scope", required = false, defaultValue = "global") scope: String
    ): ResponseEntity<List<TreeNodeDto>> {
        val filtered = metadataService.findAll()
            .filter { schema == null || it.schemaName == schema }
        return ResponseEntity.ok(buildTree(filtered, scope))
    }

    @GetMapping("/search")
    fun search(
        @RequestParam(name = "q") q: String,
        @RequestParam(name = "type", required = false) type: String?,
        @RequestParam(name = "scope", required = false, defaultValue = "global") scope: String
    ): ResponseEntity<List<SearchResultDto>> {
        val query = q.lowercase()
        val results = metadataService.findAll().asSequence()
            .filter { type == null || it.type?.name?.equals(type, ignoreCase = true) == true }
            .filter { matchesQuery(it, query) }
            .map { toSearchResult(it, scope) }
            .sortedWith(compareBy<SearchResultDto> { it.name ?: "" })
            .toList()
        return ResponseEntity.ok(results)
    }

    @GetMapping("/lineage")
    fun getLineage(
        @RequestParam(name = "table") table: String,
        @RequestParam(name = "depth", required = false, defaultValue = "1") depth: Int
    ): ResponseEntity<Map<String, Any>> {
        val lineage = mapOf(
            "table" to table,
            "depth" to depth,
            "upstream" to emptyList<String>(),
            "downstream" to emptyList<String>()
        )
        return ResponseEntity.ok(lineage)
    }

    private fun buildTree(entities: List<MetadataEntity>, scope: String): List<TreeNodeDto> {
        val bySchema = entities
            .filter { it.schemaName != null }
            .groupBy { it.schemaName!! }

        return bySchema.entries.asSequence()
            .map { (schemaName, schemaEntities) ->
                val tableEntities = schemaEntities.asSequence()
                    .filter { it.type == MetadataType.TABLE }
                    .sortedBy { it.tableName }
                    .toList()

                val tables = tableEntities.asSequence()
                    .map { tableEntity ->
                        val attributeNodes = schemaEntities.asSequence()
                            .filter { it.type == MetadataType.ATTRIBUTE }
                            .filter { it.tableName == tableEntity.tableName }
                            .sortedBy { it.attributeName }
                            .map { toTreeNode(it, scope, includeChildren = false) }
                            .toList()

                        toTreeNode(tableEntity, scope, includeChildren = true).apply {
                            children = attributeNodes
                            hasChildren = attributeNodes.isNotEmpty()
                        }
                    }
                    .sortedWith(compareBy<TreeNodeDto> { it.name ?: "" })
                    .toList()

                TreeNodeDto(
                    id = schemaName,
                    name = schemaName,
                    type = MetadataType.SCHEMA,
                    displayName = schemaName,
                    children = tables,
                    hasChildren = tables.isNotEmpty()
                )
            }
            .sortedWith(compareBy<TreeNodeDto> { it.name ?: "" })
            .toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun toTreeNode(entity: MetadataEntity, scope: String, includeChildren: Boolean): TreeNodeDto {
        val descriptive = entity.getFacet("descriptive", scope, Map::class.java)
            .map { it as Map<*, *> }
        val displayName = descriptive.map { it["displayName"] as? String }.orElse(getEntityName(entity))
        val description = descriptive.map { it["description"] as? String }.orElse(null)

        return TreeNodeDto(
            id = entity.id,
            name = getEntityName(entity),
            type = entity.type,
            displayName = displayName,
            description = description,
            children = if (includeChildren) emptyList() else null,
            hasChildren = false
        )
    }

    private fun getEntityName(entity: MetadataEntity): String =
        entity.attributeName
            ?: entity.tableName
            ?: entity.schemaName
            ?: entity.id
            ?: ""

    @Suppress("UNCHECKED_CAST")
    private fun matchesQuery(entity: MetadataEntity, query: String): Boolean {
        if (entity.id?.lowercase()?.contains(query) == true) return true
        if (entity.tableName?.lowercase()?.contains(query) == true) return true
        if (entity.attributeName?.lowercase()?.contains(query) == true) return true

        val descriptive = entity.getFacet("descriptive", "global", Map::class.java)
            .map { it as Map<*, *> }
        if (descriptive.isPresent) {
            val map = descriptive.get()
            if (map["displayName"]?.toString()?.lowercase()?.contains(query) == true) return true
            if (map["description"]?.toString()?.lowercase()?.contains(query) == true) return true
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun toSearchResult(entity: MetadataEntity, scope: String): SearchResultDto {
        val descriptive = entity.getFacet("descriptive", scope, Map::class.java)
            .map { it as Map<*, *> }
        val displayName = descriptive.map { it["displayName"] as? String }
            .orElse(entity.tableName ?: entity.id)
        val description = descriptive.map { it["description"] as? String }.orElse(null)

        return SearchResultDto(
            id = entity.id,
            name = entity.tableName ?: entity.id,
            type = entity.type,
            displayName = displayName,
            description = description,
            location = buildLocation(entity)
        )
    }

    private fun buildLocation(entity: MetadataEntity): String {
        if (entity.attributeName != null) {
            return "${entity.schemaName}.${entity.tableName}.${entity.attributeName}"
        }
        if (entity.tableName != null) {
            return "${entity.schemaName}.${entity.tableName}"
        }
        return entity.schemaName ?: entity.id ?: ""
    }
}
