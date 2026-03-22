package io.qpointz.mill.metadata.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.UrnSlug
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataChangeEvent
import io.qpointz.mill.metadata.domain.MetadataChangeObserver
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataRepository
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Instant

/**
 * Default implementation of [MetadataImportService].
 *
 * Parses multi-document YAML resources, normalises all facet-type and scope keys to URN notation,
 * saves the result via the provided repositories, and emits [MetadataChangeEvent.Imported] events
 * for each persisted entity.
 *
 * Facet type keys and scope keys in YAML may use legacy short names (`descriptive`, `global`)
 * or full URN notation; both are accepted.
 *
 * @param repository       the entity persistence store
 * @param facetTypeRepo    the facet type descriptor store
 * @param observer         change event observer; receives one [MetadataChangeEvent.Imported]
 *                         per successfully persisted entity
 */
class DefaultMetadataImportService(
    private val repository: MetadataRepository,
    private val facetTypeRepo: FacetTypeRepository,
    private val observer: MetadataChangeObserver
) : MetadataImportService {

    private val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    private val exportMapper: ObjectMapper = ObjectMapper(
        YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    ).apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
        disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun import(inputStream: InputStream, mode: ImportMode, actorId: String): ImportResult {
        val raw = inputStream.use { it.readBytes().decodeToString() }
        val documents = raw.split("\n---\n").map { it.trim() }.filter { it.isNotEmpty() }

        var entitiesImported = 0
        var facetTypesImported = 0
        val errors = mutableListOf<String>()

        var replacePerformed = false

        for (document in documents) {
            val wrapper: Map<String, Any?> = try {
                yamlMapper.readValue(document, object : TypeReference<Map<String, Any?>>() {})
            } catch (e: Exception) {
                errors.add("Failed to parse YAML document: ${e.message}")
                continue
            }

            // Handle facet-types section
            @Suppress("UNCHECKED_CAST")
            val facetTypesList = wrapper["facet-types"] as? List<Map<String, Any?>> ?: emptyList()
            for (ftMap in facetTypesList) {
                try {
                    val descriptor: FacetTypeDescriptor = yamlMapper.convertValue(ftMap, FacetTypeDescriptor::class.java)
                    descriptor.typeKey = MetadataUrns.normaliseFacetTypePath(descriptor.typeKey)
                    descriptor.applicableTo = descriptor.applicableTo
                        ?.map { UrnSlug.normalise(it, MetadataUrns.ENTITY_TYPE_PREFIX) }?.toSet()
                    facetTypeRepo.save(descriptor)
                    facetTypesImported++
                    log.info("Imported facet type: {}", descriptor.typeKey)
                } catch (e: Exception) {
                    errors.add("Failed to import facet type: ${e.message}")
                }
            }

            // Handle entities section
            @Suppress("UNCHECKED_CAST")
            val entitiesList = wrapper["entities"] as? List<Map<String, Any?>> ?: emptyList()
            if (entitiesList.isEmpty()) continue

            if (mode == ImportMode.REPLACE && !replacePerformed) {
                repository.deleteAll()
                replacePerformed = true
                log.info("REPLACE mode: cleared all existing metadata entities")
            }

            for (entityMap in entitiesList) {
                try {
                    val entity: MetadataEntity = yamlMapper.convertValue(entityMap, MetadataEntity::class.java)
                    // Normalise facet type keys and scope keys
                    entity.facets = normaliseFacetKeys(entity.facets)
                    val now = Instant.now()
                    if (entity.createdAt == null) entity.createdAt = now
                    entity.updatedAt = now
                    repository.save(entity)
                    entitiesImported++
                    observer.onEvent(MetadataChangeEvent.Imported(
                        entityId = entity.id ?: "",
                        actorId = actorId,
                        occurredAt = now,
                        entity = entity,
                        mode = mode
                    ))
                    log.info("Imported entity: {} (actor={})", entity.id, actorId)
                } catch (e: Exception) {
                    errors.add("Failed to import entity: ${e.message}")
                    log.warn("Import error for entity: {}", e.message)
                }
            }
        }

        return ImportResult(entitiesImported, facetTypesImported, errors)
    }

    override fun export(scopeKey: String): String {
        val allEntities = repository.findAll()
        val filtered = allEntities.map { entity ->
            val filteredFacets = entity.facets
                .mapValues { (_, scopeMap) ->
                    scopeMap.filterKeys { it == scopeKey }.toMutableMap()
                }
                .filter { (_, scopeMap) -> scopeMap.isNotEmpty() }
                .toMutableMap()
            MetadataEntity(
                id = entity.id,
                type = entity.type,
                schemaName = entity.schemaName,
                tableName = entity.tableName,
                attributeName = entity.attributeName,
                facets = filteredFacets,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                createdBy = entity.createdBy,
                updatedBy = entity.updatedBy
            )
        }

        val customFacetTypes = facetTypeRepo.findAll()
            .filter { !isPlatformFacetType(it.typeKey) }

        val out = ByteArrayOutputStream()
        if (customFacetTypes.isNotEmpty()) {
            exportMapper.writeValue(out, mapOf("facet-types" to customFacetTypes))
            out.write("\n---\n".toByteArray())
        }
        exportMapper.writeValue(out, mapOf("entities" to filtered))
        return out.toString(Charsets.UTF_8)
    }

    /**
     * Normalises all facet type keys and scope keys in the facets map to URN notation.
     *
     * @param facets the raw two-level facets map from deserialization
     * @return a new map with all keys normalised to URN form
     */
    private fun normaliseFacetKeys(
        facets: MutableMap<String, MutableMap<String, Any?>>
    ): MutableMap<String, MutableMap<String, Any?>> {
        val result = mutableMapOf<String, MutableMap<String, Any?>>()
        for ((facetType, scopeMap) in facets) {
            val normFacetType = MetadataUrns.normaliseFacetTypePath(facetType)
            val normScopes = mutableMapOf<String, Any?>()
            for ((scope, payload) in scopeMap) {
                normScopes[MetadataUrns.normaliseScopeKey(scope)] = payload
            }
            result[normFacetType] = normScopes
        }
        return result
    }

    /**
     * Returns `true` if [typeKey] is one of the five built-in platform facet types.
     *
     * @param typeKey the facet type key to test
     * @return `true` for platform types, `false` for custom types
     */
    private fun isPlatformFacetType(typeKey: String): Boolean = typeKey in setOf(
        MetadataUrns.FACET_TYPE_DESCRIPTIVE,
        MetadataUrns.FACET_TYPE_STRUCTURAL,
        MetadataUrns.FACET_TYPE_RELATION,
        MetadataUrns.FACET_TYPE_CONCEPT,
        MetadataUrns.FACET_TYPE_VALUE_MAPPING
    )

    companion object {
        private val log = LoggerFactory.getLogger(DefaultMetadataImportService::class.java)
    }
}
