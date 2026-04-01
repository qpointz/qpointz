package io.qpointz.mill.data.metadata.source

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.source.MetadataSource
import java.time.Instant
import java.util.UUID

/**
 * Base class for [MetadataSource] implementations that contribute only [FacetOrigin.INFERRED] rows
 * sharing a single [originId].
 */
abstract class AbstractInferredMetadataSource(
    final override val originId: String,
) : MetadataSource {

    /**
     * Builds a stable inferred facet row: deterministic [FacetInstance.assignmentUuid] from entity, type, and origin.
     *
     * @param entityId metadata entity instance URN
     * @param facetTypeKey facet type URN (canonicalised)
     * @param payload JSON-ready facet body
     */
    protected fun inferredFacet(
        entityId: String,
        facetTypeKey: String,
        payload: Map<String, Any?>,
    ): FacetInstance {
        val typeCanon = MetadataEntityUrn.canonicalize(facetTypeKey)
        val uid = UUID.nameUUIDFromBytes("$entityId|$typeCanon|$originId".toByteArray(Charsets.UTF_8)).toString()
        val now = Instant.now()
        return FacetInstance(
            assignmentUuid = uid,
            entityId = entityId,
            facetTypeKey = typeCanon,
            scopeKey = MetadataEntityUrn.canonicalize(MetadataUrns.SCOPE_GLOBAL),
            mergeAction = MergeAction.SET,
            payload = payload,
            createdAt = now,
            createdBy = null,
            lastModifiedAt = now,
            lastModifiedBy = null,
            origin = FacetOrigin.INFERRED,
            originId = originId,
            assignmentUid = null,
        )
    }
}
