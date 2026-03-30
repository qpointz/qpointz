package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeSource
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeInstEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeInstJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * JPA adapter for runtime `metadata_facet_type` rows.
 *
 * @param jpa runtime facet type Spring Data repository
 * @param defJpa definition rows (for FK wiring after [definitionAdapter] save)
 * @param definitionAdapter persists linked [io.qpointz.mill.metadata.domain.FacetTypeDefinition] rows
 */
@Transactional
class JpaRuntimeFacetTypeRepository(
    private val jpa: MetadataFacetTypeInstJpaRepository,
    private val defJpa: MetadataFacetTypeJpaRepository,
    private val definitionAdapter: JpaFacetTypeDefinitionRepository
) : FacetTypeRepository {

    override fun findByKey(typeKey: String): FacetType? {
        val k = MetadataEntityUrn.canonicalize(typeKey)
        return jpa.findByTypeRes(k).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(): List<FacetType> = jpa.findAll().map { toDomain(it) }

    override fun findDefined(): List<FacetType> =
        jpa.findAll().filter { it.source == "DEFINED" }.map { toDomain(it) }

    override fun findObserved(): List<FacetType> =
        jpa.findAll().filter { it.source == "OBSERVED" }.map { toDomain(it) }

    override fun save(facetType: FacetType): FacetType {
        val key = MetadataEntityUrn.canonicalize(facetType.typeKey)
        val now = Instant.now()
        if (facetType.definition != null) {
            definitionAdapter.save(facetType.definition!!)
        }
        val defEntity = defJpa.findByTypeRes(key).orElse(null)
        val row = jpa.findByTypeRes(key).orElse(null)
        val saved = if (row == null) {
            jpa.save(
                MetadataFacetTypeInstEntity(
                    uuid = UUID.randomUUID().toString(),
                    typeRes = key,
                    slug = null,
                    displayName = null,
                    description = null,
                    source = facetType.source.name,
                    facetTypeDef = defEntity,
                    createdAt = now,
                    createdBy = facetType.createdBy,
                    lastModifiedAt = now,
                    lastModifiedBy = facetType.lastModifiedBy
                )
            )
        } else {
            row.source = facetType.source.name
            if (defEntity != null) {
                row.facetTypeDef = defEntity
            }
            row.lastModifiedAt = now
            row.lastModifiedBy = facetType.lastModifiedBy
            jpa.save(row)
        }
        return toDomain(saved)
    }

    override fun delete(typeKey: String) {
        val k = MetadataEntityUrn.canonicalize(typeKey)
        jpa.findByTypeRes(k).ifPresent { jpa.delete(it) }
    }

    private fun toDomain(e: MetadataFacetTypeInstEntity): FacetType {
        val def = e.facetTypeDef?.let { definitionAdapter.toDomainFromDefEntity(it) }
        val source = try {
            FacetTypeSource.valueOf(e.source)
        } catch (_: Exception) {
            FacetTypeSource.OBSERVED
        }
        return FacetType(
            typeKey = e.typeRes,
            source = source,
            definition = def,
            createdAt = e.createdAt,
            createdBy = e.createdBy,
            lastModifiedAt = e.lastModifiedAt,
            lastModifiedBy = e.lastModifiedBy
        )
    }
}
