package io.qpointz.mill.metadata.repository.file

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.repository.MetadataRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Optional
import java.util.TreeMap

/** File/YAML-based metadata repository with multi-file merge support. */
class FileMetadataRepository : MetadataRepository {

    private val entities: MutableMap<String, MetadataEntity> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private val yamlMapper: ObjectMapper
    private val resourceResolver: ResourceResolver
    private val locations: List<String>

    constructor(location: String, resourceResolver: ResourceResolver)
        : this(listOf(location), resourceResolver)

    constructor(locations: List<String>, resourceResolver: ResourceResolver) {
        this.locations = locations.toList()
        this.resourceResolver = resourceResolver
        this.yamlMapper = ObjectMapper(YAMLFactory()).apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
        }
        loadEntities()
    }

    private fun loadEntities() {
        val resources = resolveFilePatterns(locations)
        if (resources.isEmpty()) {
            log.warn("No metadata files found for locations: {}", locations)
            return
        }
        var total = 0
        for (resource in resources) {
            try {
                val count = loadEntitiesFromResource(resource)
                total += count
                log.debug("Loaded {} entities from {}", count, resource.name)
            } catch (e: Exception) {
                log.error("Failed to load metadata from file: {}", resource.name, e)
                throw RuntimeException("Failed to load metadata from file: ${resource.name}", e)
            }
        }
        log.info("Loaded {} total metadata entities from {} files", total, resources.size)
    }

    private fun resolveFilePatterns(patterns: List<String>): List<ResolvedResource> {
        val resources = mutableListOf<ResolvedResource>()
        for (pattern in patterns) {
            try {
                resources.addAll(resourceResolver.resolve(pattern))
            } catch (e: Exception) {
                log.warn("Failed to resolve file pattern: {}", pattern, e)
            }
        }
        return resources
    }

    private fun loadEntitiesFromResource(resource: ResolvedResource): Int {
        resource.inputStream.use { input ->
            val fileFormat = yamlMapper.readValue(input, MetadataFileFormat::class.java)
            val list = fileFormat.entities
            if (list.isEmpty()) return 0

            var count = 0
            for (incoming in list) {
                if (incoming.createdAt == null) incoming.createdAt = Instant.now()
                if (incoming.updatedAt == null) incoming.updatedAt = Instant.now()
                if (incoming.facets == null) incoming.facets = mutableMapOf()

                val normalizedId = normalizeId(incoming.id)
                incoming.id = normalizedId
                injectTypeMarkers(incoming)

                val existing = entities[normalizedId]
                if (existing != null) {
                    mergeEntityFacets(existing, incoming)
                    existing.type = incoming.type
                    existing.schemaName = incoming.schemaName
                    existing.tableName = incoming.tableName
                    existing.attributeName = incoming.attributeName
                    existing.updatedAt = incoming.updatedAt
                    incoming.createdBy?.let { existing.createdBy = it }
                    incoming.updatedBy?.let { existing.updatedBy = it }
                } else {
                    entities[normalizedId] = incoming
                }
                count++
            }
            return count
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mergeEntityFacets(existing: MetadataEntity, incoming: MetadataEntity) {
        val inFacets = incoming.facets
        if (inFacets.isEmpty()) return

        for ((facetType, incomingScopes) in inFacets) {
            if (incomingScopes.isEmpty()) continue
            val existingScopes = existing.facets.getOrPut(facetType) { mutableMapOf() }
            for ((scope, facetData) in incomingScopes) {
                existingScopes[scope] = facetData
            }
        }
    }

    override fun save(entity: MetadataEntity) {
        entity.updatedAt = Instant.now()
        val normalizedId = normalizeId(entity.id)
        entity.id = normalizedId
        injectTypeMarkers(entity)
        entities[normalizedId] = entity
        log.debug("Saved entity: {}", normalizedId)
    }

    @Suppress("UNCHECKED_CAST")
    private fun injectTypeMarkers(entity: MetadataEntity) {
        for ((typeKey, scopedFacets) in entity.facets) {
            for ((_, data) in scopedFacets) {
                if (data is MutableMap<*, *>) {
                    (data as MutableMap<String, Any?>).putIfAbsent("_type", typeKey)
                }
            }
        }
    }

    override fun findById(id: String): Optional<MetadataEntity> =
        Optional.ofNullable(entities[normalizeId(id)])

    override fun findByLocation(schema: String?, table: String?, attribute: String?): Optional<MetadataEntity> =
        Optional.ofNullable(entities.values.firstOrNull {
            it.schemaName == schema && it.tableName == table && it.attributeName == attribute
        })

    override fun findByType(type: MetadataType): List<MetadataEntity> =
        entities.values.filter { it.type == type }

    override fun findAll(): List<MetadataEntity> =
        entities.values.toList()

    override fun deleteById(id: String) {
        val nid = normalizeId(id)
        entities.remove(nid)
        log.debug("Deleted entity: {}", nid)
    }

    override fun existsById(id: String): Boolean =
        entities.containsKey(normalizeId(id))

    private fun normalizeId(id: String?): String =
        id?.lowercase() ?: ""

    /** YAML wrapper object for `entities` array. */
    class MetadataFileFormat {
        @JsonProperty("entities")
        var entities: List<MetadataEntity> = emptyList()
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileMetadataRepository::class.java)
    }
}
