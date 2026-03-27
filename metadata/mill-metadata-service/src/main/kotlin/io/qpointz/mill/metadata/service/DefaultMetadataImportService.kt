package io.qpointz.mill.metadata.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataChangeEvent
import io.qpointz.mill.metadata.domain.MetadataChangeObserver
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifestNormalizer
import io.qpointz.mill.metadata.domain.facet.PlatformFacetTypeDefinitions
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
        val documents = splitYamlDocuments(raw)

        var entitiesImported = 0
        var facetTypesImported = 0
        val errors = mutableListOf<String>()

        var replacePerformed = false
        var facetTypesProvided = false

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
            if (facetTypesList.isNotEmpty()) facetTypesProvided = true
            for (ftMap in facetTypesList) {
                try {
                    val manifest = yamlMapper.convertValue(ftMap, FacetTypeManifest::class.java)
                    val normalized = FacetTypeManifestNormalizer.normalizeStrict(manifest)
                    val descriptor = FacetTypeDescriptor(
                        typeKey = normalized.typeKey,
                        mandatory = normalized.mandatory,
                        enabled = normalized.enabled,
                        displayName = normalized.title,
                        description = normalized.description,
                        applicableTo = normalized.applicableTo?.toSet(),
                        version = normalized.schemaVersion,
                        contentSchema = null,
                        manifestJson = yamlMapper.writeValueAsString(normalized)
                    )
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
                    // Normalise keys and transform legacy facet payloads into current platform facets.
                    entity.facets = normaliseAndTransformFacets(entity.type, entity.facets)
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

        if (!facetTypesProvided) {
            facetTypesImported += ensurePlatformFacetTypesPresent()
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
            val manifests = customFacetTypes.map { descriptor ->
                parseOrBuildManifestMap(descriptor)
            }
            exportMapper.writeValue(out, mapOf("facet-types" to manifests))
            out.write("\n---\n".toByteArray())
        }
        exportMapper.writeValue(out, mapOf("entities" to filtered))
        return out.toString(Charsets.UTF_8)
    }

    private fun parseOrBuildManifestMap(descriptor: FacetTypeDescriptor): Map<String, Any?> {
        val raw = descriptor.manifestJson
        if (!raw.isNullOrBlank()) {
            return yamlMapper.readValue(raw, object : TypeReference<Map<String, Any?>>() {})
        }
        return mapOf(
            "typeKey" to descriptor.typeKey,
            "title" to (descriptor.displayName ?: descriptor.typeKey),
            "description" to (descriptor.description ?: "Facet type descriptor"),
            "enabled" to descriptor.enabled,
            "mandatory" to descriptor.mandatory,
            "applicableTo" to (descriptor.applicableTo?.toList() ?: emptyList<String>()),
            "schemaVersion" to descriptor.version,
            "payload" to mapOf(
                "type" to FacetSchemaType.OBJECT.name,
                "title" to "${descriptor.displayName ?: descriptor.typeKey} payload",
                "description" to "Payload schema",
                "fields" to emptyList<Any>(),
                "required" to emptyList<String>()
            )
        )
    }

    /**
     * Normalises all facet type keys and scope keys in the facets map to URN notation.
     *
     * @param facets the raw two-level facets map from deserialization
     * @return a new map with all keys normalised to URN form
     */
    private fun normaliseAndTransformFacets(
        entityType: MetadataType?,
        facets: MutableMap<String, MutableMap<String, Any?>>
    ): MutableMap<String, MutableMap<String, Any?>> {
        val result = mutableMapOf<String, MutableMap<String, Any?>>()
        for ((facetType, scopeMap) in facets) {
            val normFacetType = MetadataUrns.normaliseFacetTypePath(facetType)
            for ((scope, payload) in scopeMap) {
                // Import playground always uses GLOBAL scope on startup import.
                val globalScope = MetadataUrns.SCOPE_GLOBAL
                when (normFacetType) {
                    MetadataUrns.FACET_TYPE_DESCRIPTIVE -> {
                        val mapped = mapDescriptivePayload(payload)
                        result.getOrPut(MetadataUrns.FACET_TYPE_DESCRIPTIVE) { mutableMapOf() }[globalScope] = mapped
                    }
                    MetadataUrns.FACET_TYPE_STRUCTURAL -> {
                        mapStructuralPayload(entityType, payload)?.let { (targetFacetType, mapped) ->
                            result.getOrPut(targetFacetType) { mutableMapOf() }[globalScope] = mapped
                        }
                    }
                    MetadataUrns.FACET_TYPE_RELATION -> {
                        val mapped = mapRelationPayload(payload)
                        result.getOrPut(MetadataUrns.FACET_TYPE_RELATION) { mutableMapOf() }[globalScope] = mapped
                    }
                    else -> {
                        result.getOrPut(normFacetType) { mutableMapOf() }[globalScope] = payload
                    }
                }
            }
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapDescriptivePayload(payload: Any?): Any? {
        val m = payload as? Map<String, Any?> ?: return payload
        val out = mutableMapOf<String, Any?>()
        if (m.containsKey("displayName")) out["displayName"] = m["displayName"]
        if (m.containsKey("description")) out["description"] = m["description"]
        return out
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapStructuralPayload(entityType: MetadataType?, payload: Any?): Pair<String, Any?>? {
        val m = payload as? Map<String, Any?> ?: return null
        return when (entityType) {
            MetadataType.TABLE -> {
                val out = mutableMapOf<String, Any?>(
                    "sourceType" to "FLOW",
                    "package" to "",
                    "name" to (m["physicalName"] ?: "")
                )
                MetadataUrns.normaliseFacetTypePath("source-table") to out
            }
            MetadataType.ATTRIBUTE -> {
                val out = mutableMapOf<String, Any?>(
                    "name" to (m["physicalName"] ?: ""),
                    "type" to (m["physicalType"] ?: ""),
                    "nullable" to ((m["nullable"] as? Boolean) ?: false),
                    "isFK" to ((m["isForeignKey"] as? Boolean) ?: false),
                    "isPK" to ((m["isPrimaryKey"] as? Boolean) ?: false)
                )
                MetadataUrns.normaliseFacetTypePath("source-column") to out
            }
            else -> {
                null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapRelationPayload(payload: Any?): Any? {
        val m = payload as? Map<String, Any?> ?: return payload
        val relations = m["relations"] as? List<Map<String, Any?>> ?: return payload
        return relations.map { relation ->
            val rawCardinality = (relation["cardinality"] as? String)?.trim()?.uppercase().orEmpty()
            val cardinality = when (rawCardinality) {
                "ONE_TO_ONE", "ONE_TO_MANY", "MANY_TO_MANY" -> rawCardinality
                else -> "UNKNOWN"
            }
            mapOf(
                "name" to relation["name"],
                "description" to relation["description"],
                "cardinality" to cardinality,
                "source" to mapOf(
                    "schema" to (relation["sourceTable"] as? Map<String, Any?>)?.get("schema"),
                    "table" to (relation["sourceTable"] as? Map<String, Any?>)?.get("table"),
                    "columns" to ((relation["sourceAttributes"] as? List<*>)?.map { it.toString() } ?: emptyList<String>())
                ),
                "target" to mapOf(
                    "schema" to (relation["targetTable"] as? Map<String, Any?>)?.get("schema"),
                    "table" to (relation["targetTable"] as? Map<String, Any?>)?.get("table"),
                    "columns" to ((relation["targetAttributes"] as? List<*>)?.map { it.toString() } ?: emptyList<String>())
                ),
                "expression" to relation["joinSql"]
            )
        }
    }

    /**
     * Returns `true` if [typeKey] is one of the five built-in platform facet types.
     *
     * @param typeKey the facet type key to test
     * @return `true` for platform types, `false` for custom types
     */
    private fun isPlatformFacetType(typeKey: String): Boolean = typeKey in PlatformFacetTypeDefinitions.typeKeys()

    private fun splitYamlDocuments(raw: String): List<String> =
        raw.split(Regex("(?m)^---\\s*$"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun ensurePlatformFacetTypesPresent(): Int {
        val platformDescriptors = PlatformFacetTypeDefinitions.manifests().map { platformDescriptor(it) }
        var added = 0
        for (descriptor in platformDescriptors) {
            if (!facetTypeRepo.existsByTypeKey(descriptor.typeKey)) {
                facetTypeRepo.save(descriptor)
                added++
                log.info("Ensured platform facet type: {}", descriptor.typeKey)
            }
        }
        return added
    }

    private fun platformDescriptor(manifest: FacetTypeManifest): FacetTypeDescriptor {
        return FacetTypeDescriptor(
            typeKey = manifest.typeKey,
            mandatory = manifest.mandatory,
            targetCardinality = manifest.targetCardinality,
            enabled = manifest.enabled,
            displayName = manifest.title,
            description = manifest.description,
            applicableTo = manifest.applicableTo?.toSet(),
            version = manifest.schemaVersion,
            contentSchema = null,
            manifestJson = yamlMapper.writeValueAsString(manifest)
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DefaultMetadataImportService::class.java)
    }
}
