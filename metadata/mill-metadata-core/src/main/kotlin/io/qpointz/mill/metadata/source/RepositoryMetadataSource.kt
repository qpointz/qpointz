package io.qpointz.mill.metadata.source

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.toCapturedReadModel
import io.qpointz.mill.metadata.repository.FacetReadSide
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.metadata.service.MetadataReader

/**
 * [MetadataSource] backed by persisted [FacetAssignment] rows: loads, merges scopes, emits [FacetInstance] with [io.qpointz.mill.metadata.domain.facet.FacetOrigin.CAPTURED].
 *
 * @param facetReadSide persisted assignment reads
 * @param metadataReader scope merge for effective rows
 */
class RepositoryMetadataSource(
    private val facetReadSide: FacetReadSide,
    private val metadataReader: MetadataReader
) : MetadataSource {

    override val originId: String = ORIGIN_ID

    override fun fetchForEntity(entityId: String, context: MetadataReadContext): List<FacetInstance> {
        if (!context.isOriginActive(originId)) {
            return emptyList()
        }
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val all = facetReadSide.findByEntity(eid)
        val merged = metadataReader.resolveEffective(all, context)
        return merged.map { it.toCapturedReadModel(originId) }
    }

    companion object {
        /** Default [MetadataSource.originId] for repository-backed facets. */
        const val ORIGIN_ID: String = MetadataOriginIds.REPOSITORY_LOCAL
    }
}
