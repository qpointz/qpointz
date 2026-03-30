package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.io.MetadataYamlSerializer
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import java.io.Writer

/**
 * @param entityRepository source of entity rows
 * @param facetRepository source of facet assignment rows
 * @param definitionRepository facet type definitions
 * @param scopeRepository scope registry
 */
class DefaultMetadataSnapshotService(
    private val entityRepository: MetadataEntityRepository,
    private val facetRepository: FacetRepository,
    private val definitionRepository: FacetTypeDefinitionRepository,
    private val scopeRepository: MetadataScopeRepository
) : MetadataSnapshotService {

    override fun snapshotAll(out: Writer) {
        val entities = entityRepository.findAll()
        val facetsByEntity = entities.associate { e ->
            val id = MetadataEntityUrn.canonicalize(e.id)
            id to facetRepository.findByEntity(id)
        }
        val yaml = MetadataYamlSerializer.serialize(
            scopes = scopeRepository.findAll(),
            definitions = definitionRepository.findAll(),
            entities = entities,
            facetsByEntity = facetsByEntity
        )
        out.write(yaml)
    }

    override fun snapshotEntities(entityIds: List<String>, out: Writer) {
        val ids = entityIds.map { MetadataEntityUrn.canonicalize(it) }.distinct()
        val entities = ids.mapNotNull { entityRepository.findById(it) }
        val facetsByEntity = entities.associate { e ->
            val id = MetadataEntityUrn.canonicalize(e.id)
            id to facetRepository.findByEntity(id)
        }
        val scopeKeys = facetsByEntity.values.flatten().map { MetadataEntityUrn.canonicalize(it.scopeKey) }.toSet()
        val defKeys = facetsByEntity.values.flatten().map { MetadataEntityUrn.canonicalize(it.facetTypeKey) }.toSet()
        val scopes = scopeKeys.mapNotNull { scopeRepository.findByRes(it) }
        val definitions = defKeys.mapNotNull { definitionRepository.findByKey(it) }
        val yaml = MetadataYamlSerializer.serialize(
            scopes = scopes,
            definitions = definitions,
            entities = entities,
            facetsByEntity = facetsByEntity
        )
        out.write(yaml)
    }
}
