package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.ConceptTarget
import io.qpointz.mill.metadata.domain.core.EntityReference
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.metadata.repository.MetadataRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Optional

/** Application service for metadata lookup, mutation, and relation traversal. */
open class MetadataService @JvmOverloads constructor(
    private val repository: MetadataRepository,
    private val facetCatalog: FacetCatalog? = null
) {

    fun getFacetCatalog(): Optional<FacetCatalog> = Optional.ofNullable(facetCatalog)

    fun findById(id: String): Optional<MetadataEntity> = repository.findById(id)

    fun findByLocation(schema: String?, table: String?, attribute: String?): Optional<MetadataEntity> =
        repository.findByLocation(schema, table, attribute)

    fun findByType(type: MetadataType): List<MetadataEntity> = repository.findByType(type)

    fun findAll(): List<MetadataEntity> = repository.findAll()

    fun save(entity: MetadataEntity) {
        if (facetCatalog != null) {
            val result = facetCatalog.validateEntityFacets(entity)
            if (!result.valid) {
                throw IllegalArgumentException("Facet validation failed: ${result.errors.joinToString("; ")}")
            }
        }
        if (entity.createdAt == null) entity.createdAt = Instant.now()
        entity.updatedAt = Instant.now()
        repository.save(entity)
    }

    fun deleteById(id: String) = repository.deleteById(id)

    fun <T : Any> getFacet(entityId: String, facetType: String, scope: String, facetClass: Class<T>): Optional<T> =
        repository.findById(entityId).flatMap { it.getFacet(facetType, scope, facetClass) }

    fun <T : Any> getMergedFacet(
        entityId: String, facetType: String,
        userId: String, userTeams: List<String>?, userRoles: List<String>?,
        facetClass: Class<T>
    ): Optional<T> = repository.findById(entityId).flatMap {
        it.getMergedFacet(facetType, userId, userTeams ?: emptyList(), userRoles ?: emptyList(), facetClass)
    }

    fun setFacet(entityId: String, facetType: String, scope: String, facetData: Any?) {
        val entity = repository.findById(entityId)
            .orElseThrow { IllegalArgumentException("Entity not found: $entityId") }
        entity.setFacet(facetType, scope, facetData)
        save(entity)
    }

    fun findRelatedEntities(entityId: String, scope: String): List<MetadataEntity> {
        val selectedEntity = repository.findById(entityId).orElse(null) ?: return emptyList()
        val relatedIds = mutableSetOf<String>()

        val allConcepts = repository.findByType(MetadataType.CONCEPT)
        for (concept in allConcepts) {
            val cf = concept.getFacet("concept", scope, ConceptFacet::class.java).orElse(null) ?: continue
            outer@for (c in cf.concepts) {
                for (target in c.targets) {
                    if (matchesEntity(selectedEntity, target.schema, target.table, null)) {
                        relatedIds.add(concept.id!!)
                        break@outer
                    }
                    if (selectedEntity.attributeName != null && target.attributes.contains(selectedEntity.attributeName)) {
                        relatedIds.add(concept.id!!)
                        break@outer
                    }
                }
            }
        }

        val allEntities = repository.findAll()
        for (entity in allEntities) {
            val rf = entity.getFacet("relation", scope, RelationFacet::class.java).orElse(null) ?: continue
            for (relation in rf.relations) {
                if (matchesEntityReference(selectedEntity, relation.sourceTable) ||
                    matchesEntityReference(selectedEntity, relation.targetTable)) {
                    relatedIds.add(entity.id!!)
                    relation.targetTable?.let { tt ->
                        val tid = buildEntityId(tt)
                        if (tid != null && tid != entityId) {
                            repository.findById(tid).ifPresent { relatedIds.add(it.id!!) }
                        }
                    }
                }
            }
        }

        selectedEntity.getFacet("relation", scope, RelationFacet::class.java).ifPresent { rf ->
            for (relation in rf.relations) {
                relation.targetTable?.let { tt ->
                    val tid = buildEntityId(tt)
                    if (tid != null && tid != entityId) {
                        repository.findById(tid).ifPresent { relatedIds.add(it.id!!) }
                    }
                }
            }
        }

        selectedEntity.getFacet("concept", scope, ConceptFacet::class.java).ifPresent { cf ->
            for (concept in cf.concepts) {
                for (target in concept.targets) {
                    repository.findByLocation(target.schema, target.table, null)
                        .ifPresent { relatedIds.add(it.id!!) }
                    for (attr in target.attributes) {
                        repository.findByLocation(target.schema, target.table, attr)
                            .ifPresent { relatedIds.add(it.id!!) }
                    }
                }
            }
        }

        return relatedIds
            .mapNotNull { repository.findById(it).orElse(null) }
            .filter { it.id != entityId }
    }

    private fun matchesEntity(entity: MetadataEntity, schema: String?, table: String?, attribute: String?): Boolean =
        entity.schemaName == schema && entity.tableName == table &&
                (attribute == null || entity.attributeName == attribute)

    private fun matchesEntityReference(entity: MetadataEntity, ref: EntityReference?): Boolean =
        ref?.matches(entity.schemaName, entity.tableName, entity.attributeName) ?: false

    private fun buildEntityId(ref: EntityReference): String? {
        if (ref.schema == null || ref.table == null) return null
        return if (!ref.attribute.isNullOrEmpty()) "${ref.schema}.${ref.table}.${ref.attribute}"
        else "${ref.schema}.${ref.table}"
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataService::class.java)
    }
}
