package io.qpointz.mill.persistence.metadata.jpa.adapters

import tools.jackson.core.type.TypeReference
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeDefEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import io.qpointz.mill.utils.JsonUtils
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Maps [FacetTypeDefinition] to `manifest_json` plus row-level `mandatory` / `enabled` flags.
 *
 * @param jpa Spring Data repository for `metadata_facet_type_def`
 */
@Transactional
class JpaFacetTypeDefinitionRepository(
    private val jpa: MetadataFacetTypeJpaRepository
) : FacetTypeDefinitionRepository {

    private val mapper = JsonUtils.defaultJsonMapper()

    override fun findByKey(typeKey: String): FacetTypeDefinition? =
        jpa.findByTypeRes(MetadataEntityUrn.canonicalize(typeKey)).map { toDomain(it) }.orElse(null)

    override fun findAll(): List<FacetTypeDefinition> = jpa.findAll().map { toDomain(it) }

    override fun save(definition: FacetTypeDefinition): FacetTypeDefinition {
        val key = MetadataEntityUrn.canonicalize(definition.typeKey)
        val now = Instant.now()
        val manifestJson = defToManifest(definition)
        val row = jpa.findByTypeRes(key).orElse(null)
        val saved = if (row == null) {
            jpa.save(
                MetadataFacetTypeDefEntity(
                    uuid = UUID.randomUUID().toString(),
                    typeRes = key,
                    manifestJson = manifestJson,
                    mandatory = definition.mandatory,
                    enabled = definition.enabled,
                    createdAt = now,
                    createdBy = definition.createdBy,
                    lastModifiedAt = now,
                    lastModifiedBy = definition.lastModifiedBy
                )
            )
        } else {
            row.manifestJson = manifestJson
            row.mandatory = definition.mandatory
            row.enabled = definition.enabled
            row.lastModifiedAt = now
            row.lastModifiedBy = definition.lastModifiedBy
            jpa.save(row)
        }
        return toDomain(saved)
    }

    override fun delete(typeKey: String) {
        jpa.findByTypeRes(MetadataEntityUrn.canonicalize(typeKey)).ifPresent { jpa.delete(it) }
    }

    private fun defToManifest(d: FacetTypeDefinition): String {
        val m = linkedMapOf<String, Any?>(
            "facetTypeUrn" to d.typeKey,
            "displayName" to d.displayName,
            "description" to d.description,
            "category" to d.category,
            "mandatory" to d.mandatory,
            "enabled" to d.enabled,
            "targetCardinality" to d.targetCardinality.name,
            "applicableTo" to d.applicableTo,
            "contentSchema" to d.contentSchema,
            "schemaVersion" to d.schemaVersion
        )
        return mapper.writeValueAsString(m)
    }

    internal fun toDomainFromDefEntity(entity: MetadataFacetTypeDefEntity): FacetTypeDefinition = toDomain(entity)

    private fun toDomain(e: MetadataFacetTypeDefEntity): FacetTypeDefinition {
        val map: Map<String, Any?> = try {
            mapper.readValue(e.manifestJson, object : TypeReference<Map<String, Any?>>() {})
        } catch (_: Exception) {
            emptyMap()
        }
        val cardRaw = map["targetCardinality"]?.toString() ?: "SINGLE"
        val card = try {
            FacetTargetCardinality.valueOf(cardRaw)
        } catch (_: Exception) {
            FacetTargetCardinality.SINGLE
        }
        val applicable = (map["applicableTo"] as? List<*>)?.map { it.toString() }
        @Suppress("UNCHECKED_CAST")
        val contentSchema = (map["contentSchema"] ?: map["payload"]) as? Map<String, Any?>
        val displayName = map["displayName"]?.toString()?.takeIf { it.isNotEmpty() }
            ?: map["title"]?.toString()?.takeIf { it.isNotEmpty() }
        return FacetTypeDefinition(
            typeKey = e.typeRes,
            displayName = displayName,
            description = map["description"]?.toString(),
            category = map["category"]?.toString()?.takeIf { it.isNotBlank() },
            mandatory = e.mandatory,
            enabled = e.enabled,
            targetCardinality = card,
            applicableTo = applicable,
            contentSchema = contentSchema,
            schemaVersion = map["schemaVersion"]?.toString(),
            createdAt = e.createdAt,
            createdBy = e.createdBy,
            lastModifiedAt = e.lastModifiedAt,
            lastModifiedBy = e.lastModifiedBy
        )
    }
}
