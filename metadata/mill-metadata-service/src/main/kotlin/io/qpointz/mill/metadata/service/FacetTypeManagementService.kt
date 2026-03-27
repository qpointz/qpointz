package io.qpointz.mill.metadata.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifestNormalizer
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Facet type management orchestration for the metadata REST API.
 *
 * Keeps controllers transport-thin by:
 * - enforcing strict manifest normalization/validation
 * - mapping domain/catalog constraints to Mill status exceptions
 */
@Service
class FacetTypeManagementService(
    private val facetCatalog: FacetCatalog,
    private val facetTypeRepository: FacetTypeRepository,
    private val objectMapper: ObjectMapper
) {

    private val strictMapper: ObjectMapper = objectMapper.copy().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
    }

    fun list(targetType: String?, enabledOnly: Boolean): List<FacetTypeManifest> {
        val descriptors = if (targetType != null) {
            facetCatalog.getForTargetType(targetType)
        } else if (enabledOnly) {
            facetCatalog.getEnabled()
        } else {
            facetCatalog.getAll()
        }
        val filtered = if (enabledOnly && targetType != null) descriptors.filter { it.enabled } else descriptors
        return filtered.map { descriptorToManifest(it) }
    }

    fun get(typeKeyUrn: String): FacetTypeManifest =
        facetCatalog.get(typeKeyUrn)
            .map { descriptorToManifest(it) }
            .orElseThrow { MillStatuses.notFoundRuntime("Facet type not found: $typeKeyUrn") }

    fun create(rawManifest: FacetTypeManifest): FacetTypeManifest {
        val normalizedTypeKey = MetadataUrns.normaliseFacetTypePath(rawManifest.typeKey)
        val aliases = linkedSetOf(normalizedTypeKey, rawManifest.typeKey.trim())
        if (normalizedTypeKey.startsWith(MetadataUrns.FACET_TYPE_PREFIX)) {
            aliases.add(normalizedTypeKey.removePrefix(MetadataUrns.FACET_TYPE_PREFIX))
        }
        val conflictingAlias = aliases.firstOrNull { it.isNotBlank() && facetTypeRepository.existsByTypeKey(it) }
        if (conflictingAlias != null) {
            throw MillStatuses.conflictRuntime(
                "Facet type key already exists (alias: $conflictingAlias, normalized: $normalizedTypeKey)"
            )
        }

        val manifest = FacetTypeManifestNormalizer.normalizeStrict(rawManifest.copy(typeKey = normalizedTypeKey))
        val descriptor = manifestToDescriptor(manifest, Instant.now(), null)
        try {
            facetCatalog.register(descriptor)
        } catch (e: IllegalArgumentException) {
            throw MillStatuses.conflictRuntime(e.message ?: "Facet type conflict")
        }
        return manifest
    }

    fun update(typeKeyUrn: String, rawManifest: FacetTypeManifest): FacetTypeManifest {
        val existing = facetCatalog.get(typeKeyUrn).orElseThrow {
            MillStatuses.notFoundRuntime("Facet type not found: $typeKeyUrn")
        }
        val normalized = FacetTypeManifestNormalizer.normalizeStrict(rawManifest.copy(typeKey = typeKeyUrn))
        val descriptor = manifestToDescriptor(normalized, Instant.now(), existing.createdAt)
        try {
            facetCatalog.update(descriptor)
        } catch (e: IllegalArgumentException) {
            // catalog currently uses IllegalArgumentException for constraints
            throw MillStatuses.badRequestRuntime(e.message ?: "Invalid facet type update")
        }
        return normalized
    }

    fun delete(typeKeyUrn: String) {
        val existing = facetCatalog.get(typeKeyUrn).orElseThrow {
            MillStatuses.notFoundRuntime("Facet type not found: $typeKeyUrn")
        }
        if (existing.mandatory) throw MillStatuses.conflictRuntime("Facet type is mandatory and cannot be deleted: $typeKeyUrn")
        val inUse = facetTypeRepository.usageCount(typeKeyUrn)
        if (inUse > 0) throw MillStatuses.conflictRuntime("Facet type is in use ($inUse references): $typeKeyUrn")
        facetCatalog.delete(typeKeyUrn)
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

    private fun descriptorToManifest(descriptor: FacetTypeDescriptor): FacetTypeManifest {
        val json = descriptor.manifestJson ?: throw MillStatuses.internalErrorRuntime(
            "Facet type manifestJson is missing for ${descriptor.typeKey}"
        )
        return try {
            strictMapper.readValue(json, FacetTypeManifest::class.java)
        } catch (e: Exception) {
            throw MillStatuses.internalErrorRuntime(
                "Facet type manifestJson is invalid for ${descriptor.typeKey}: ${e.message}"
            )
        }
    }

    private fun manifestToDescriptor(manifest: FacetTypeManifest, now: Instant, createdAt: Instant?): FacetTypeDescriptor {
        val manifestJson = strictMapper.writeValueAsString(manifest)
        return FacetTypeDescriptor(
            typeKey = MetadataUrns.normaliseFacetTypePath(manifest.typeKey),
            mandatory = manifest.mandatory,
            targetCardinality = manifest.targetCardinality,
            enabled = manifest.enabled,
            displayName = manifest.title,
            description = manifest.description,
            applicableTo = manifest.applicableTo?.toSet(),
            version = manifest.schemaVersion,
            contentSchema = null,
            manifestJson = manifestJson,
            createdAt = createdAt ?: now,
            updatedAt = now
        )
    }
}

