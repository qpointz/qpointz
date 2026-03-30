package io.qpointz.mill.metadata.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifestNormalizer
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Facet type management orchestration for the metadata REST API.
 *
 * Uses [FacetCatalog] and [FacetTypeDefinitionRepository] for persistence-shaped definitions,
 * [FacetRepository] for usage counts, and strict JSON parsing for request bodies.
 */
@Service
class FacetTypeManagementService(
    private val facetCatalog: FacetCatalog,
    private val definitionRepository: FacetTypeDefinitionRepository,
    private val facetTypeRepository: FacetTypeRepository,
    private val facetRepository: FacetRepository,
    private val objectMapper: ObjectMapper
) {

    private val strictMapper: ObjectMapper = objectMapper.copy().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
    }

    fun list(targetType: String?, enabledOnly: Boolean): List<FacetTypeManifest> {
        var defs = facetCatalog.listDefinitions()
        if (enabledOnly) {
            defs = defs.filter { it.enabled }
        }
        if (targetType != null) {
            defs = defs.filter { d ->
                val app = d.applicableTo
                app.isNullOrEmpty() || app.any { a -> a.equals(targetType, ignoreCase = true) }
            }
        }
        return defs.map { definitionToManifest(it) }
    }

    fun get(typeKeyUrn: String): FacetTypeManifest {
        val key = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKeyUrn))
        val def = facetCatalog.findDefinition(key)
            ?: throw MillStatuses.notFoundRuntime("Facet type not found: $typeKeyUrn")
        return definitionToManifest(def)
    }

    fun create(rawManifest: FacetTypeManifest): FacetTypeManifest {
        val normalizedTypeKey = MetadataUrns.normaliseFacetTypePath(rawManifest.typeKey)
        val canonical = MetadataEntityUrn.canonicalize(normalizedTypeKey)
        val aliases = linkedSetOf(canonical, normalizedTypeKey, rawManifest.typeKey.trim())
        if (normalizedTypeKey.startsWith(MetadataUrns.FACET_TYPE_PREFIX)) {
            aliases.add(normalizedTypeKey.removePrefix(MetadataUrns.FACET_TYPE_PREFIX))
        }
        val conflictingAlias = aliases.firstOrNull { a ->
            if (a.isBlank()) return@firstOrNull false
            val lookupKey = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(a))
            definitionRepository.findByKey(lookupKey) != null || facetTypeRepository.findByKey(lookupKey) != null
        }
        if (conflictingAlias != null) {
            throw MillStatuses.conflictRuntime(
                "Facet type key already exists (alias: $conflictingAlias, normalized: $canonical)"
            )
        }
        val manifest = FacetTypeManifestNormalizer.normalizeStrict(rawManifest.copy(typeKey = normalizedTypeKey))
        val now = Instant.now()
        val def = manifestToDefinition(manifest, now, "api")
        try {
            facetCatalog.registerDefinition(def)
        } catch (e: IllegalArgumentException) {
            throw MillStatuses.conflictRuntime(e.message ?: "Facet type conflict")
        }
        return manifest
    }

    fun update(typeKeyUrn: String, rawManifest: FacetTypeManifest): FacetTypeManifest {
        val key = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKeyUrn))
        val existing = facetCatalog.findDefinition(key)
            ?: throw MillStatuses.notFoundRuntime("Facet type not found: $typeKeyUrn")
        val normalized = FacetTypeManifestNormalizer.normalizeStrict(rawManifest.copy(typeKey = key))
        val now = Instant.now()
        val def = manifestToDefinition(normalized, now, "api").copy(
            createdAt = existing.createdAt,
            createdBy = existing.createdBy,
            lastModifiedAt = now,
            lastModifiedBy = "api"
        )
        try {
            facetCatalog.registerDefinition(def)
        } catch (e: IllegalArgumentException) {
            throw MillStatuses.badRequestRuntime(e.message ?: "Invalid facet type update")
        }
        return normalized
    }

    fun delete(typeKeyUrn: String) {
        val key = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKeyUrn))
        val existing = facetCatalog.findDefinition(key)
            ?: throw MillStatuses.notFoundRuntime("Facet type not found: $typeKeyUrn")
        if (existing.mandatory) {
            throw MillStatuses.conflictRuntime("Facet type is mandatory and cannot be deleted: $typeKeyUrn")
        }
        val inUse = facetRepository.countByFacetType(key)
        if (inUse > 0) {
            throw MillStatuses.conflictRuntime("Facet type is in use ($inUse references): $typeKeyUrn")
        }
        definitionRepository.delete(key)
        facetTypeRepository.delete(key)
    }

    fun parseJson(body: String, contentType: MediaType?): FacetTypeManifest {
        if (contentType != null && !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            throw MillStatuses.badRequestRuntime("Unsupported content type: $contentType")
        }
        return try {
            strictMapper.readValue(body, FacetTypeManifest::class.java)
        } catch (e: Exception) {
            throw MillStatuses.badRequestRuntime("Malformed facet type manifest JSON: ${e.message}")
        }
    }

    private fun definitionToManifest(def: FacetTypeDefinition): FacetTypeManifest {
        val payload: FacetPayloadSchema = if (def.contentSchema != null) {
            try {
                objectMapper.convertValue(def.contentSchema, FacetPayloadSchema::class.java)
            } catch (_: Exception) {
                emptyPayloadSchema(def)
            }
        } else {
            emptyPayloadSchema(def)
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
            payload = payload
        )
    }

    private fun emptyPayloadSchema(def: FacetTypeDefinition): FacetPayloadSchema = FacetPayloadSchema(
        type = FacetSchemaType.OBJECT,
        title = def.displayName ?: def.typeKey,
        description = def.description ?: ""
    )

    private fun manifestToDefinition(manifest: FacetTypeManifest, now: Instant, actor: String): FacetTypeDefinition {
        val key = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(manifest.typeKey))
        @Suppress("UNCHECKED_CAST")
        val contentSchema = objectMapper.convertValue(manifest.payload, Map::class.java) as Map<String, Any?>
        return FacetTypeDefinition(
            typeKey = key,
            displayName = manifest.title,
            description = manifest.description,
            category = manifest.category,
            mandatory = manifest.mandatory,
            enabled = manifest.enabled,
            targetCardinality = manifest.targetCardinality,
            applicableTo = manifest.applicableTo,
            contentSchema = contentSchema,
            schemaVersion = manifest.schemaVersion,
            createdAt = now,
            createdBy = actor,
            lastModifiedAt = now,
            lastModifiedBy = actor
        )
    }
}
